package ru.set404.clients.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.services.TherapistService;
import ru.set404.clients.util.AppointmentModelAssembler;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class TherapistController {

    private final AppointmentModelAssembler appointmentModelAssembler;
    private final TherapistService therapistService;

    @Autowired
    public TherapistController(AppointmentModelAssembler appointmentModelAssembler, TherapistService therapistService) {
        this.appointmentModelAssembler = appointmentModelAssembler;
        this.therapistService = therapistService;
    }

    /* todo */
    @GetMapping("therapist/{id}/appointments")
    public CollectionModel<EntityModel<Appointment>> all(@PathVariable Long id) {
        List<Appointment> appointments = therapistService.findAll(id);
        return appointmentModelAssembler.toCollectionModel(appointments);
    }

    @GetMapping("therapist/{id}/appointments/{appointmentId}")
    public EntityModel<Appointment> getById(@PathVariable Long id, @PathVariable Long appointmentId) {
        Appointment appointment = therapistService.getById(id, appointmentId);
        return appointmentModelAssembler.toModel(appointment);
    }

    @DeleteMapping("/therapist/appointments/{appointmentId}")
    public ResponseEntity<?> delete(@PathVariable Long appointmentId) {
        therapistService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/therapist")
    ResponseEntity<EntityModel<Therapist>> newTherapist(@RequestBody Therapist therapist) {
        Therapist newTherapist = therapistService.saveTherapist(therapist);
        return ResponseEntity
                .created(linkTo(methodOn(TherapistController.class).all(newTherapist.getId())).toUri())
                .body(EntityModel.of(therapist));
    }

}
