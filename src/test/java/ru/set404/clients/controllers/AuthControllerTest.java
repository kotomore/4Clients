package ru.set404.clients.controllers;


import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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
import ru.set404.clients.dto.TherapistDTO;
import ru.set404.clients.security.JwtRequest;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ClientsApplication.class})
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@Sql(scripts = {"classpath:delete-data.sql", "classpath:init-data.sql"})
public class AuthControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void whenLoginValidCredentials() throws Exception {
        TherapistDTO therapist = createTestTherapist();

        JwtRequest request = new JwtRequest();
        request.setLogin(therapist.getPhone());
        request.setPassword(therapist.getPassword());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(200)).andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    public void whenLoginInvalidUsername() throws Exception {
        TherapistDTO therapist = createTestTherapist();

        JwtRequest request = new JwtRequest();
        request.setLogin("000" + therapist.getPhone());
        request.setPassword(therapist.getPassword());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(403)).andExpect(jsonPath("$.message", is("Authorization error. User with phone - " + request.getLogin() + " not found")));
    }

    @Test
    public void whenLoginInvalidPassword() throws Exception {
        TherapistDTO therapist = createTestTherapist();

        JwtRequest request = new JwtRequest();
        request.setLogin(therapist.getPhone());
        request.setPassword("Invalid" + therapist.getPassword());

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(403)).andExpect(jsonPath("$.message", is("Authorization error. Wrong password")));
    }

    private TherapistDTO createTestTherapist() throws Exception {
        TherapistDTO therapistDTO = new TherapistDTO("Bob", "88005553535", "qwerty");
        mvc.perform(post("/therapists/create").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(therapistDTO)))
                .andExpect(status().is(201));
        return therapistDTO;
    }
}
