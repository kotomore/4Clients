package ru.set404.clients.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.ServiceDTO;
import ru.set404.clients.dto.TherapistDTO;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Availability;
import ru.set404.clients.models.Service;
import ru.set404.clients.models.Therapist;
import ru.set404.clients.services.TherapistService;
import ru.set404.clients.util.AppointmentModelAssembler;
import ru.set404.clients.util.TherapistModelAssembler;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/therapists")
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

    @GetMapping("/{id}")
    public EntityModel<Therapist> getTherapistById(@PathVariable Long id) {
        Therapist therapist = therapistService.getTherapist(id);
        return therapistModelAssembler.toModel(therapist);
    }

    @GetMapping("/all")
    public CollectionModel<EntityModel<Therapist>> allTherapists() {
        List<Therapist> therapists = therapistService.getAllTherapist();
        return therapistModelAssembler.toCollectionModel(therapists);
    }

    @PostMapping
    ResponseEntity<EntityModel<TherapistDTO>> newTherapist(@RequestBody TherapistDTO therapist) {
        Long newTherapistId = therapistService.saveTherapist(therapist);
        return ResponseEntity
                .created(linkTo(methodOn(TherapistController.class).getTherapistById(newTherapistId)).toUri())
                .body(EntityModel.of(therapist));
    }

    @PutMapping
    ResponseEntity<?> updateTherapist(@RequestBody Therapist newTherapist) {
        Therapist updatedTherapist = therapistService.getTherapist(newTherapist.getId());
        updatedTherapist.setName(newTherapist.getName());
        updatedTherapist.setPassword(newTherapist.getPassword());
        updatedTherapist.setPhone(newTherapist.getPhone());
        updatedTherapist.setRole(newTherapist.getRole());
        therapistService.updateTherapist(updatedTherapist);
        EntityModel<Therapist> entityModel = therapistModelAssembler.toModel(updatedTherapist);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTherapist(@PathVariable Long id) {
        therapistService.deleteTherapist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/appointments/{appointmentId}")
    public EntityModel<Appointment> getAppointmentById(@PathVariable Long id, @PathVariable Long appointmentId) {
        Appointment appointment = therapistService.getAppointmentById(id, appointmentId);
        return appointmentModelAssembler.toModel(appointment);
    }

    @GetMapping("/{id}/appointments")
    public CollectionModel<EntityModel<Appointment>> allappointments(@PathVariable Long id) {
        List<Appointment> appointments = therapistService.findAll(id);
        return appointmentModelAssembler.toCollectionModel(appointments);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long appointmentId) {
        therapistService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/availabilities")
    ResponseEntity<EntityModel<Therapist>> newAvailableTime(@PathVariable Long id, @RequestBody Availability availability) {
        therapistService.addAvailableTime(id, availability.getDate(), availability.getStartTime(), availability.getEndTime());
        return ResponseEntity
                .created(linkTo(methodOn(ClientController.class).availableTimes(id, availability.getDate())).toUri())
                .build();
    }

    @DeleteMapping("/{id}/availabilities")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long id, @RequestBody LocalDate date) {
        therapistService.deleteAvailableTime(id, date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/services")
    public EntityModel<Service> getService(@PathVariable Long id) {
        Service service = therapistService.getService(id);
        return EntityModel.of(service, linkTo(methodOn(TherapistController.class)
                .getTherapistById(id)).withRel("therapist"));
    }

    @PostMapping("/{id}/services")
    ResponseEntity<EntityModel<Service>> newService(@PathVariable Long id, @RequestBody ServiceDTO service) {
        therapistService.addOrUpdateService(id, service);
        return ResponseEntity
                .created(linkTo(methodOn(TherapistController.class).getService(id)).toUri())
                .build();
    }
}
