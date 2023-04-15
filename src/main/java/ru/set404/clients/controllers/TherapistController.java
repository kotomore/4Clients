package ru.set404.clients.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.dto.*;
import ru.set404.clients.models.*;
import ru.set404.clients.security.TherapistDetails;
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
@RequiredArgsConstructor
@RequestMapping("/therapists")
@CrossOrigin(allowedHeaders = {"Authorization", "Origin"}, value = "*")
public class TherapistController {

    private final AppointmentModelAssembler appointmentModelAssembler;
    private final TherapistModelAssembler therapistModelAssembler;
    private final ClientModelAssembler clientModelAssembler;
    private final TherapistService therapistService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @GetMapping()
    public EntityModel<Therapist> getCurrentTherapist() {
        Long therapistId = getAuthUserId();
        Therapist therapist = therapistService.findTherapistById(therapistId);
        return therapistModelAssembler.toModel(therapist);
    }

    @PutMapping
    ResponseEntity<?> updateTherapist(@Valid @RequestBody TherapistDTO newTherapist) {
        Long therapistId = getAuthUserId();
        Therapist updatedTherapist = therapistService.findTherapistById(therapistId);
        updatedTherapist.setName(newTherapist.getName());
        updatedTherapist.setPassword(passwordEncoder.encode(newTherapist.getPassword()));
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
        Appointment appointment = therapistService.findAppointmentById(therapistId, appointmentId);
        return EntityModel.of(appointment);
    }

    @GetMapping("/appointments")
    public CollectionModel<EntityModel<AppointmentDTO>> allAppointments() {
        Long therapistId = getAuthUserId();
        List<AppointmentDTO> appointments = therapistService.findAllAppointments(therapistId);
        return appointmentModelAssembler.toCollectionModel(appointments);
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> deleteAppointment(@PathVariable Long appointmentId) {
        Long therapistId = getAuthUserId();
        therapistService.deleteAppointment(therapistId, appointmentId);
        return ResponseEntity.noContent().build();
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

    @GetMapping("/availabilities")
    public ResponseEntity<?> availableTimes(@RequestParam LocalDate date) {
        Long therapistId = getAuthUserId();
        List<LocalTime> availableTimes = therapistService.findAvailableTimes(therapistId, date);
        if (availableTimes.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available time for appointment to date - " + date));
        return new ResponseEntity<>(availableTimes, HttpStatus.OK);
    }

    @PostMapping("/availabilities")
    ResponseEntity<EntityModel<Therapist>> newAvailableTime(@Valid @RequestBody AvailabilityDTO availabilityDTO) {
        Long therapistId = getAuthUserId();
        therapistService.addAvailableTime(therapistId, modelMapper.map(availabilityDTO, Availability.class));
        return ResponseEntity
                .ok()
                .build();
    }

    @PostMapping("/availabilities/few")
    ResponseEntity<EntityModel<Therapist>> newAvailableTime(@Valid @RequestBody AvailabilitiesDTO availabilitiesDTO) {
        Long therapistId = getAuthUserId();
        therapistService.addAvailableTime(therapistId, availabilitiesDTO);
        return ResponseEntity
                .ok()
                .build();
    }

    @DeleteMapping("/availabilities")
    public ResponseEntity<?> deleteAppointment(@RequestParam LocalDate date) {
        Long therapistId = getAuthUserId();
        therapistService.deleteAvailableTime(therapistId, date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services")
    public EntityModel<Service> getService() {
        Long therapistId = getAuthUserId();
        Service service = therapistService.findService(therapistId);
        return EntityModel.of(service, linkTo(methodOn(TherapistController.class)
                .getCurrentTherapist()).withRel("therapist"));
    }

    @PostMapping("/services")
    public EntityModel<Service> newService(@Valid @RequestBody ServiceDTO serviceDTO) {
        Long therapistId = getAuthUserId();
        therapistService.addOrUpdateService(therapistId, serviceDTO);
        Service service = therapistService.findService(therapistId);
        return EntityModel.of(service, linkTo(methodOn(TherapistController.class)
                .getCurrentTherapist()).withRel("therapist"));
    }

    @GetMapping("/clients")
    public CollectionModel<EntityModel<Client>> getClients() {
        Long therapistId = getAuthUserId();
        List<Client> clients = therapistService.findClients(therapistId);
        return clientModelAssembler.toCollectionModel(clients);
    }

    private Long getAuthUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((TherapistDetails) authentication.getPrincipal()).getTherapist().getId();
    }
}
