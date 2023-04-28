package ru.set404.clients.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.AgentServiceDTO;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.services.ClientService;

import javax.management.ServiceNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

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
        Set<LocalTime> availableTimes = clientService.findAvailableTimes(agentId, date);
        return new ResponseEntity<>(availableTimes, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/availableDates")
    public ResponseEntity<?> availableDates(@RequestParam String agentId, @RequestParam LocalDate date) {

        Set<LocalDate> availableDates = clientService.findAvailableDates(agentId, date);
        return new ResponseEntity<>(availableDates, HttpStatus.OK);
    }

    @CrossOrigin
    @GetMapping("/services")
    public ResponseEntity<?> getService(@RequestParam String agentId) throws ServiceNotFoundException {
        AgentServiceDTO service = clientService.findService(agentId);
        return new ResponseEntity<>(service, HttpStatus.OK);
    }
}
