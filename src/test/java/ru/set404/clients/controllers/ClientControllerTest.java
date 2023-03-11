package ru.set404.clients.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.set404.clients.ClientsApplication;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.dto.ClientDTO;
import ru.set404.clients.dto.ServiceDTO;
import ru.set404.clients.models.Availability;
import ru.set404.clients.models.Role;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.services.RegistrationService;
import ru.set404.clients.services.TherapistService;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ClientsApplication.class})
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Sql(scripts = {"classpath:delete-data.sql", "classpath:init-data.sql"})
public class ClientControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TherapistService service;

    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAvailableTimes() throws Exception {
        mvc.perform(get("/clients/availableTimes").contentType(MediaType.APPLICATION_JSON)
                        .param("therapistId", "1")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.length()", is(23)));
    }

    @Test
    public void getAvailableTimesForUnavailableDate() throws Exception {
        mvc.perform(get("/clients/availableTimes").contentType(MediaType.APPLICATION_JSON)
                        .param("therapistId", "1")
                        .param("date", LocalDate.now().plusDays(30).toString()))
                .andExpect(status().is(406));
    }

    @Test
    public void getAvailableTimesForUnknownTherapist() throws Exception {
        mvc.perform(get("/clients/availableTimes").contentType(MediaType.APPLICATION_JSON)
                        .param("therapistId", "100500")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is(406));
    }

    @Test
    public void getAvailableDates() throws Exception {
        mvc.perform(get("/clients/availableDates").contentType(MediaType.APPLICATION_JSON)
                        .param("therapistId", "1")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.length()", is(1)));
    }

    @Test
    public void getAvailableDatesForUnavailableDate() throws Exception {
        mvc.perform(get("/clients/availableDates").contentType(MediaType.APPLICATION_JSON)
                        .param("therapistId", "1")
                        .param("date", LocalDate.now().plusDays(40).toString()))
                .andExpect(status().is(406));
    }

    @Test
    public void getAvailableDatesForUnknownTherapist() throws Exception {
        mvc.perform(get("/clients/availableDates").contentType(MediaType.APPLICATION_JSON)
                        .param("therapistId", "100500")
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is(406));
    }

    @Test
    public void newAppointment() throws Exception {
        Long therapistId = 1L;
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setName("Client");
        clientDTO.setPhone("8800555");
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setClient(clientDTO);
        appointmentDTO.setServiceId(1L);
        appointmentDTO.setTherapistId(therapistId);
        appointmentDTO.setStartTime(service.findAvailableTimes(therapistId,
                LocalDate.now().plusDays(1))
                .get(0)
                .atDate(LocalDate.now().plusDays(1)));

        mvc.perform(post("/clients/appointment").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentDTO)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.appointmentId", is(1)));
    }

    @Before
    public void createTestTherapist() {
        Therapist therapist = new Therapist("Bob", "88005553535", "qwerty", Role.USER);
        Long therapistId = registrationService.saveTherapist(therapist);
        ServiceDTO serviceDTO = new ServiceDTO("Name", "Description", 60, 5000);

        service.addOrUpdateService(therapistId, serviceDTO);
        Availability availability = new Availability();
        availability.setDate(LocalDate.now().plusDays(1));
        availability.setStartTime(LocalTime.of(0, 0));
        availability.setEndTime(LocalTime.of(23, 0));
        service.addAvailableTime(therapistId, availability);
    }
}
