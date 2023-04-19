package ru.set404.clients.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.AgentServiceDTO;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.models.AgentService;
import ru.set404.clients.services.ClientService;

import javax.management.ServiceNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients")
public class ClientController {

private final ClientService clientService;

    @PostMapping("/appointment")
    public ResponseEntity<?> newAppointment(@Valid @RequestBody AppointmentDTO newAppointment) {
        clientService.createAppointment(newAppointment);
        EntityModel<AppointmentDTO> entityModel = EntityModel.of(newAppointment);
        return ResponseEntity
                .ok()
                .body(entityModel);
    }

    @CrossOrigin
    @GetMapping("/availableTimes")
    public ResponseEntity<?> availableTimes(@RequestParam String agentId, @RequestParam LocalDate date) {
        List<LocalTime> availableTimes = clientService.findAvailableTimes(agentId, date);
        if (availableTimes.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available time for appointment to date - " + date));
        return new ResponseEntity<>(availableTimes, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/availableDates")
    public ResponseEntity<?> availableDates(@RequestParam String agentId, @RequestParam LocalDate date) {

        List<LocalDate> availableDates = clientService.findAvailableDates(agentId, date);

        if (availableDates.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available date for appointment to month - " + date.getMonth()));
        return new ResponseEntity<>(availableDates, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/services")
    public ResponseEntity<?> getService(@RequestParam String agentId) throws ServiceNotFoundException {
        AgentServiceDTO service = clientService.findService(agentId);
        return new ResponseEntity<>(service, HttpStatus.OK);
    }
}
