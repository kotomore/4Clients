package ru.set404.clients.controllers;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Availability;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.services.TherapistService;
import ru.set404.clients.util.AppointmentModelAssembler;
import ru.set404.clients.util.TherapistModelAssembler;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class TherapistController {

    private final AppointmentModelAssembler appointmentModelAssembler;
    private final TherapistModelAssembler therapistModelAssembler;
    private final TherapistService therapistService;

    @Autowired
    public TherapistController(AppointmentModelAssembler appointmentModelAssembler, TherapistModelAssembler therapistModelAssembler, TherapistService therapistService) {
        this.appointmentModelAssembler = appointmentModelAssembler;
        this.therapistModelAssembler = therapistModelAssembler;
        this.therapistService = therapistService;
    }

    /* todo */
    @GetMapping("therapist/{id}/appointments")
    public CollectionModel<EntityModel<Appointment>> all(@PathVariable Long id) {
        List<Appointment> appointments = therapistService.findAll(id);
        return appointmentModelAssembler.toCollectionModel(appointments);
    }

    @GetMapping("therapist/all")
    public CollectionModel<EntityModel<Therapist>> allTherapists() {
        List<Therapist> appointments = therapistService.getAllTherapist();
        return therapistModelAssembler.toCollectionModel(appointments);
    }

    @GetMapping("therapist/{id}/appointments/{appointmentId}")
    public EntityModel<Appointment> getAppointmentById(@PathVariable Long id, @PathVariable Long appointmentId) {
        Appointment appointment = therapistService.getAppointmentById(id, appointmentId);
        return appointmentModelAssembler.toModel(appointment);
    }

    @DeleteMapping("/therapist/appointments/{appointmentId}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long appointmentId) {
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

    @GetMapping("therapist/{id}")
    public EntityModel<Therapist> getTherapistById(@PathVariable Long id) {
        Therapist therapist = therapistService.getTherapist(id);
        return therapistModelAssembler.toModel(therapist);
    }

    @PutMapping("/therapist/{id}")
    ResponseEntity<?> updateTherapist(@RequestBody Therapist newTherapist, @PathVariable Long id) {
        Therapist updatedTherapist = therapistService.getTherapist(id);
        updatedTherapist.setName(newTherapist.getName());
        updatedTherapist.setPassword(newTherapist.getPassword());
        updatedTherapist.setPhone(newTherapist.getPhone());
        updatedTherapist.setRole(newTherapist.getRole());
        EntityModel<Therapist> entityModel = therapistModelAssembler.toModel(updatedTherapist);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PostMapping("/therapist/{id}/availability")
    ResponseEntity<EntityModel<Therapist>> newAvailableTime(@PathVariable Long id, @RequestBody Availability availability) {
        therapistService.addAvailableTime(id, availability.getDate(), availability.getStartTime(), availability.getEndTime());
        return ResponseEntity
                .created(linkTo(methodOn(ClientController.class).availableTimes(id, availability.getDate())).toUri())
                .build();
    }

}
