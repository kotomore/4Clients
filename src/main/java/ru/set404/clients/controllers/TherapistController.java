package ru.set404.clients.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.services.TherapistService;
import ru.set404.clients.util.AppointmentModelAssembler;

import java.util.List;

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
}
