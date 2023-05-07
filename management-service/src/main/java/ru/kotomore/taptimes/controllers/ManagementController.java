package ru.kotomore.taptimes.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.kotomore.taptimes.dto.TimeSlotDTO;
import ru.kotomore.taptimes.models.Appointment;
import ru.kotomore.taptimes.security.AgentDetails;
import ru.kotomore.taptimes.dto.AgentDTO;
import ru.kotomore.taptimes.dto.AgentServiceDTO;
import ru.kotomore.taptimes.models.AgentService;
import ru.kotomore.taptimes.models.Client;
import ru.kotomore.taptimes.services.ManagementService;
import ru.kotomore.taptimes.util.AgentModelAssembler;
import ru.kotomore.taptimes.util.AppointmentModelAssembler;
import ru.kotomore.taptimes.util.ClientModelAssembler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agents")
@CrossOrigin(allowedHeaders = {"Authorization", "Origin"}, value = "*")
public class ManagementController {

    private final AppointmentModelAssembler appointmentModelAssembler;
    private final AgentModelAssembler agentModelAssembler;
    private final ClientModelAssembler clientModelAssembler;
    private final ManagementService managementService;

    @GetMapping()
    public ResponseEntity<?> getCurrentAgent() {
        String agentId = getAuthUserId();
        if (agentId != null) {
            AgentDTO agent = managementService.findAgentDTOById(agentId);
            return ResponseEntity.ok().body(agent);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping
    ResponseEntity<?> updateAgent(@Valid @RequestBody AgentDTO newAgent) {
        String agentId = getAuthUserId();
        managementService.updateAgent(agentId, newAgent);

        EntityModel<AgentDTO> entityModel = agentModelAssembler.toModel(newAgent);
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping()
    public ResponseEntity<?> deleteAgent() {
        String agentId = getAuthUserId();
        managementService.deleteTherapist(agentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments/{appointmentId}")
    public EntityModel<Appointment> getAppointmentById(@PathVariable String appointmentId) {
        String agentId = getAuthUserId();
        Appointment appointment = managementService.findAppointmentById(agentId, appointmentId);
        return EntityModel.of(appointment);
    }

    @GetMapping("/appointments")
    public CollectionModel<EntityModel<Appointment>> allAppointments() {
        String agentId = getAuthUserId();
        return appointmentModelAssembler.toCollectionModel(managementService.findAllAppointments(agentId));
    }

    @DeleteMapping("/appointments/{appointmentId}")
    public ResponseEntity<?> deleteAppointment(@PathVariable String appointmentId) {
        String agentId = getAuthUserId();
        managementService.deleteAppointment(agentId, appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availabilities")
    public ResponseEntity<?> availableTimes(@RequestParam LocalDate date) {
        String agentId = getAuthUserId();
        Set<LocalTime> availableTimes = managementService.findAvailableTimes(agentId, date);
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
    ResponseEntity<?> newAvailableTime(@Valid @RequestBody TimeSlotDTO timeSlotDTO) {
        final long MAX_DAYS_TO_ADD = 30;
        if (timeSlotDTO.getDateEnd().toEpochDay() - timeSlotDTO.getDateStart().toEpochDay() > MAX_DAYS_TO_ADD) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("The date range")
                            .withDetail("The date range should be within " + MAX_DAYS_TO_ADD + " days"));
        }
        String agentId = getAuthUserId();
        managementService.addAvailableTime(agentId, timeSlotDTO);
        return ResponseEntity
                .ok()
                .build();
    }

    @DeleteMapping("/availabilities")
    public ResponseEntity<?> deleteAppointment(@RequestParam LocalDate date) {
        String agentId = getAuthUserId();
        managementService.deleteAvailableTime(agentId, date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services")
    public EntityModel<AgentService> getService() {
        String agentId = getAuthUserId();
        AgentService service = managementService.findService(agentId);
        return EntityModel.of(service, linkTo(methodOn(ManagementController.class)
                .getCurrentAgent()).withRel("agent"));
    }

    @PostMapping("/services")
    public EntityModel<AgentService> newService(@Valid @RequestBody AgentServiceDTO service) {
        String agentId = getAuthUserId();
        AgentService savedService = managementService.addOrUpdateService(agentId, service);
        return EntityModel.of(savedService, linkTo(methodOn(ManagementController.class)
                .getCurrentAgent()).withRel("agent"));
    }

    @GetMapping("/clients")
    public CollectionModel<EntityModel<Client>> getClients() {
        String agentId = getAuthUserId();
        List<Client> clients = managementService.findClients(agentId);
        return clientModelAssembler.toCollectionModel(clients);
    }

    private String getAuthUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return ((AgentDetails) authentication.getPrincipal()).getAgent().getId();
        } catch (ClassCastException ex) {
            return null;
        }

    }
}
