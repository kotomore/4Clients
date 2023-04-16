package ru.set404.clients.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.AppointmentDTO;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.services.ClientService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/appointment")
    public ResponseEntity<?> newAppointment(@Valid @RequestBody AppointmentDTO newAppointment) {

        Appointment appointment = clientService.addAppointment(newAppointment);

        if (appointment == null)
            return ResponseEntity
                    .status(412)
                    .body("Date is not available");

        EntityModel<Appointment> entityModel = EntityModel.of(appointment);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @CrossOrigin
    @GetMapping("/availableTimes")
    public ResponseEntity<?> availableTimes(@RequestParam Long therapistId, @RequestParam LocalDate date) {
        List<LocalTime> availableTimes = clientService.findAvailableTimes(therapistId, date);
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
    public ResponseEntity<?> availableDates(@RequestParam Long therapistId, @RequestParam LocalDate date) {
        List<LocalDate> availableDates = clientService.findAvailableDates(therapistId, date);
        if (availableDates.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available date for appointment to month - " + date.getMonth()));
        return new ResponseEntity<>(availableDates, HttpStatus.OK);
    }
}
