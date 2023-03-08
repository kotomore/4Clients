package ru.set404.clients.controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.security.auth.message.AuthException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
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
import ru.set404.clients.dto.TherapistDTO;
import ru.set404.clients.exceptions.TimeNotAvailableException;
import ru.set404.clients.models.Availabilities;
import ru.set404.clients.models.Availability;
import ru.set404.clients.models.Role;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.security.JwtRequest;
import ru.set404.clients.services.TherapistService;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ClientsApplication.class})
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Sql(scripts = {"classpath:delete-data.sql", "classpath:init-data.sql"})
public class TherapistControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TherapistService service;
    @Autowired
    private ObjectMapper objectMapper;

    private String getAccessToken() throws Exception {
        TherapistDTO therapist = createTestTherapist();

        JwtRequest request = new JwtRequest();
        request.setLogin(therapist.getPhone());
        request.setPassword(therapist.getPassword());

        String response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andReturn().getResponse().getContentAsString();
        return new JSONObject(response).get("accessToken").toString();
    }

    @Test
    public void getTherapistAfterLogin() throws Exception {
        mvc.perform(get("/therapists").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getAccessToken())
                )
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name", is("Bob")));
        ;
    }

    @Test
    public void getTherapistWithoutLogin() throws Exception {
        mvc.perform(get("/therapists").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403));
        ;
    }

    @Test
    public void createTherapist() throws Exception {
        TherapistDTO therapist = new TherapistDTO("Bob", "88005553535", "qwerty");
        mvc.perform(post("/therapists/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(therapist)))
                .andExpect(status().is(201));
    }

    @Test
    public void updateTherapist() throws Exception {
        Therapist therapist = new Therapist("John", "12345", "12345", Role.USER);
        mvc.perform(put("/therapists").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getAccessToken())
                        .content(objectMapper.writeValueAsString(therapist)))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.name", is(therapist.getName())));
    }

    @Test
    public void deleteTherapist() throws Exception {
        mvc.perform(delete("/therapists").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + getAccessToken()))
                .andExpect(status().is(204));
        assertThrows(AuthException.class, () -> service.findTherapistByPhone("88005553535"));
    }

    @Test
    public void deleteAppointmentById() throws Exception {
        String token = getAccessToken();
        createAppointment();
        mvc.perform(delete("/therapists/appointments/1").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(204));
    }

    @Test
    public void getAppointmentById() throws Exception {
        String token = getAccessToken();
        AppointmentDTO appointmentDTO = createAppointment();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        mvc.perform(get("/therapists/appointments/1").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.startTime", is(appointmentDTO.getStartTime().format(formatter))));
    }

    @Test
    public void getAppointments() throws Exception {
        String token = getAccessToken();
        AppointmentDTO appointmentDTO = createAppointment();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        mvc.perform(get("/therapists/appointments").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$._embedded.appointmentList[0].startTime", is(appointmentDTO.getStartTime().format(formatter))));
    }

    @Test
    public void getAppointmentByDate() throws Exception {
        String token = getAccessToken();
        AppointmentDTO appointmentDTO = createAppointment();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        mvc.perform(get("/therapists/appointments/byDate").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0].start", is(appointmentDTO.getStartTime().format(formatter))));
    }

    @Test
    public void getAvailabilities() throws Exception {
        String token = getAccessToken();
        createAppointment();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;
        mvc.perform(get("/therapists/availabilities").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$[0]", is(LocalTime.of(0, 0).format(formatter))));
    }

    @Test
    public void addAvailability() throws Exception {
        String token = getAccessToken();
        createAppointment();

        Availability availability = new Availability();
        availability.setDate(LocalDate.now().plusDays(5));
        availability.setStartTime(LocalTime.of(0, 0));
        availability.setEndTime(LocalTime.of(23, 0));

        mvc.perform(post("/therapists/availabilities").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(availability)))
                .andExpect(status().is(200));
        assertTrue((service.getAvailableDates(1L, LocalDate.now().plusDays(5)).size() > 0));
    }

    @Test
    public void addAvailabilities() throws Exception {
        String token = getAccessToken();
        createAppointment();

        Availabilities availability = new Availabilities();
        availability.setStartTime(LocalDate.now().plusDays(4).atTime(0, 0));
        availability.setEndTime(LocalDate.now().plusDays(5).atTime(23, 0));

        mvc.perform(post("/therapists/availabilities/few").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(availability)))
                .andExpect(status().is(200));
        assertTrue((service.getAvailableDates(1L, LocalDate.now().plusDays(5)).size() > 0));
    }

    @Test
    public void deleteAvailability() throws Exception {
        String token = getAccessToken();
        createAppointment();

        assertTrue(service.getAvailableDates(1L, LocalDate.now().plusDays(1)).size() > 0);

        mvc.perform(delete("/therapists/availabilities").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .param("date", LocalDate.now().plusDays(1).toString()))
                .andExpect(status().is(204));

        assertThrows(TimeNotAvailableException.class, () -> service.getAvailableDates(1L, LocalDate.now().plusDays(1)));
    }

    @Test
    public void getService() throws Exception {
        String token = getAccessToken();
        AppointmentDTO appointmentDTO = createAppointment();
        mvc.perform(get("/therapists/services").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.serviceId", is(appointmentDTO.getServiceId().intValue())));
    }

    @Test
    public void updateService() throws Exception {
        String token = getAccessToken();
        createAppointment();
        ServiceDTO serviceDTO = new ServiceDTO("DifferentName", "DifferentDescription", 30, 2000);

        mvc.perform(post("/therapists/services").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(serviceDTO)))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name", is(serviceDTO.getName())));
    }

    @Test
    public void getClients() throws Exception {
        String token = getAccessToken();
        createAppointment();
        mvc.perform(get("/therapists/clients").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$._embedded.clientList[0].name",
                        is("JohnDoe")));
    }

    private TherapistDTO createTestTherapist() throws Exception {
        TherapistDTO therapistDTO = new TherapistDTO("Bob", "88005553535", "qwerty");
        mvc.perform(post("/therapists/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(therapistDTO)))
                .andExpect(status().is(201));
        return therapistDTO;
    }

    private AppointmentDTO createAppointment() throws AuthException {
        ClientDTO client = new ClientDTO();
        client.setName("JohnDoe");
        client.setPhone("88001234567");

        Therapist therapist = service.findTherapistByPhone("88005553535");
        Long therapistId = therapist.getId();

        ServiceDTO serviceDTO = new ServiceDTO("Name", "Description", 60, 5000);
        service.addOrUpdateService(therapistId, serviceDTO);

        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setTherapistId(therapistId);
        appointmentDTO.setClient(client);
        appointmentDTO.setServiceId(service.getService(therapistId).getServiceId());
        appointmentDTO.setStartTime(LocalDate.now().plusDays(1).atTime(10, 0));

        service.addAvailableTime(therapistId, LocalDate.now().plusDays(1), LocalTime.of(0, 0), LocalTime.of(23, 0));
        service.addAppointment(appointmentDTO);

        return appointmentDTO;
    }
}
