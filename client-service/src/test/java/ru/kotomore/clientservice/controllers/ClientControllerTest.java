package ru.kotomore.clientservice.controllers;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.kotomore.clientservice.dto.AgentServiceDTO;
import ru.kotomore.clientservice.dto.AppointmentDTO;
import ru.kotomore.clientservice.services.ClientService;

import javax.management.ServiceNotFoundException;
import java.time.LocalDate;
import java.util.Set;

import static org.mockito.Mockito.when;

public class ClientControllerTest {
    @Mock
    private ClientService clientService;
    @InjectMocks
    private ClientController clientController;

    AutoCloseable openMocks;
    public ClientControllerTest() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNewAppointment() {
        AppointmentDTO newAppointment = new AppointmentDTO();
        newAppointment.setAgentId("agent-123");
        ResponseEntity<?> responseEntity = clientController.newAppointment(newAppointment);

        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(responseEntity.getBody() instanceof EntityModel<?>);
    }

    @Test
    public void testAvailableTimes() {
        String agentId = "agent-123";
        LocalDate date = LocalDate.now();
        ResponseEntity<?> responseEntity = clientController.availableTimes(agentId, date);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(responseEntity.getBody() instanceof Set<?>);
    }

    @Test
    public void testAvailableDates() {
        String agentId = "agent-123";
        LocalDate date = LocalDate.now();
        ResponseEntity<?> responseEntity = clientController.availableDates(agentId, date);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(responseEntity.getBody() instanceof Set<?>);
    }

    @Test
    public void testGetService() throws ServiceNotFoundException {
        String agentId = "agent-123";

        AgentServiceDTO service = new AgentServiceDTO();
        service.setId("123");
        service.setName("Service Name");
        service.setDuration(60);
        service.setPrice(5000);
        service.setAgentPhone("8800");
        service.setAgentName("John Doe");

        when(clientService.findService(agentId)).thenReturn(service);

        ResponseEntity<?> responseEntity = clientController.getService(agentId);
        Assert.assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
        Assert.assertTrue(responseEntity.getBody() instanceof AgentServiceDTO);
        Assert.assertEquals(service.getName(), ((AgentServiceDTO) responseEntity.getBody()).getName());
    }
}