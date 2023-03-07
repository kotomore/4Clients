package ru.set404.clients.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.AppointmentsForSiteDTO;
import ru.set404.clients.dto.ServiceDTO;
import ru.set404.clients.dto.TherapistDTO;
import ru.set404.clients.models.*;
import ru.set404.clients.services.TherapistService;
import ru.set404.clients.util.AppointmentModelAssembler;
import ru.set404.clients.util.ClientModelAssembler;
import ru.set404.clients.util.TherapistModelAssembler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/therapists")
@CrossOrigin(allowedHeaders = {"Authorization", "Origin"}, value = "*")
public class TherapistController {

    private final AppointmentModelAssembler appointmentModelAssembler;
    private final TherapistModelAssembler therapistModelAssembler;
    private final ClientModelAssembler clientModelAssembler;
    private final TherapistService therapistService;

    @Autowired
    public TherapistController(AppointmentModelAssembler appointmentModelAssembler, TherapistModelAssembler therapistModelAssembler, ClientModelAssembler clientModelAssembler, TherapistService therapistService) {
        this.appointmentModelAssembler = appointmentModelAssembler;
        this.therapistModelAssembler = therapistModelAssembler;
        this.clientModelAssembler = clientModelAssembler;
        this.therapistService = therapistService;
    }

    @GetMapping()
    public EntityModel<Therapist> getTherapistById() {
        Long therapistId = getAuthUserId();
        Therapist therapist = therapistService.getTherapist(therapistId);
        return therapistModelAssembler.toModel(therapist);
    }

    @GetMapping("/all")
    public CollectionModel<EntityModel<Therapist>> allTherapists() {
        List<Therapist> therapists = therapistService.getAllTherapist();
        return therapistModelAssembler.toCollectionModel(therapists);
    }

    @PostMapping
    @CrossOrigin
    ResponseEntity<EntityModel<TherapistDTO>> newTherapist(@RequestBody TherapistDTO therapist) {
        therapistService.saveTherapist(therapist);
        return ResponseEntity
                .created(linkTo(methodOn(TherapistController.class).getTherapistById()).toUri())
                .body(EntityModel.of(therapist));
    }

    @PutMapping
    ResponseEntity<?> updateTherapist(@RequestBody Therapist newTherapist) {
        Long therapistId = getAuthUserId();
        Therapist updatedTherapist = therapistService.getTherapist(therapistId);
        updatedTherapist.setName(newTherapist.getName());
        updatedTherapist.setPassword(newTherapist.getPassword());
        updatedTherapist.setPhone(newTherapist.getPhone());
        updatedTherapist.setRole(Role.USER);
        therapistService.updateTherapist(updatedTherapist);
        EntityModel<Therapist> entityModel = therapistModelAssembler.toModel(updatedTherapist);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteTherapist() {
        Long therapistId = getAuthUserId();
        therapistService.deleteTherapist(therapistId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments/{appointmentId}")
    public EntityModel<Appointment> getAppointmentById(@PathVariable Long appointmentId) {
        Long therapistId = getAuthUserId();
        Appointment appointment = therapistService.getAppointmentById(therapistId, appointmentId);
        return appointmentModelAssembler.toModel(appointment);
    }

    @GetMapping("/appointments")
    public CollectionModel<EntityModel<Appointment>> allAppointments() {
        Long therapistId = getAuthUserId();
        List<Appointment> appointments = therapistService.findAllAppointments(therapistId);
        return appointmentModelAssembler.toCollectionModel(appointments);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long appointmentId) {
        Long therapistId = getAuthUserId();
        therapistService.deleteAppointment(therapistId, appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availabilities")
    public ResponseEntity<?> availableTimes(@RequestParam LocalDate date) {
        List<LocalTime> availableTimes = therapistService.getAvailableTimes(1L, date);
        if (availableTimes.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available time for appointment to date - " + date));
        return new ResponseEntity<>(availableTimes, HttpStatus.OK);
    }

    @GetMapping("/appointments/byDate")
    public ResponseEntity<?> availableTime(@RequestParam LocalDate date) {
        Long therapistId = getAuthUserId();
        List<AppointmentsForSiteDTO> appointmentsForSiteDTOS = therapistService.findAllAppointmentsDTO(therapistId);
        if (appointmentsForSiteDTOS.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available time for appointment to date - " + date));
        return new ResponseEntity<>(appointmentsForSiteDTOS, HttpStatus.OK);
    }
    @PostMapping("/availabilities")
    ResponseEntity<EntityModel<Therapist>> newAvailableTime(@RequestBody Availability availability) {
        Long therapistId = getAuthUserId();
        therapistService.addAvailableTime(therapistId, availability.getDate(), availability.getStartTime(), availability.getEndTime());
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/availabilities/few")
    ResponseEntity<EntityModel<Therapist>> newAvailableTime(@RequestBody Availabilities availabilities) {
        Long therapistId = getAuthUserId();
        therapistService.addAvailableTime(therapistId, availabilities.getStartTime(), availabilities.getEndTime());
        return ResponseEntity
                .ok()
                .build();
    }

    @DeleteMapping("/availabilities")
    public ResponseEntity<?> deleteAppointment(@RequestBody LocalDate date) {
        Long therapistId = getAuthUserId();
        therapistService.deleteAvailableTime(therapistId, date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services")
    public EntityModel<Service> getService() {
        Long therapistId = getAuthUserId();
        Service service = therapistService.getService(therapistId);
        return EntityModel.of(service, linkTo(methodOn(TherapistController.class)
                .getTherapistById()).withRel("therapist"));
    }

    @PostMapping("/services")
    ResponseEntity<EntityModel<Service>> newService(@RequestBody ServiceDTO service) {
        Long therapistId = getAuthUserId();
        therapistService.addOrUpdateService(therapistId, service);
        return ResponseEntity
                .ok()
                .build();
    }

    @GetMapping("/clients")
    public CollectionModel<EntityModel<Client>> getClients() {
        Long therapistId = getAuthUserId();
        List<Client> clients = therapistService.getClients(therapistId);
        return clientModelAssembler.toCollectionModel(clients);
    }

    private Long getAuthUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
}
