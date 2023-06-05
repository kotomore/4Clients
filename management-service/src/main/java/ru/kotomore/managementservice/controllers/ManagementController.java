package ru.kotomore.managementservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
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
import ru.kotomore.managementservice.dto.AgentResponseDTO;
import ru.kotomore.managementservice.dto.TimeSlotDTO;
import ru.kotomore.managementservice.models.Appointment;
import ru.kotomore.managementservice.security.AgentDetails;
import ru.kotomore.managementservice.dto.AgentRequestDTO;
import ru.kotomore.managementservice.dto.AgentServiceDTO;
import ru.kotomore.managementservice.models.AgentService;
import ru.kotomore.managementservice.models.Client;
import ru.kotomore.managementservice.services.ManagementService;
import ru.kotomore.managementservice.util.AgentModelAssembler;
import ru.kotomore.managementservice.util.AppointmentModelAssembler;
import ru.kotomore.managementservice.util.ClientModelAssembler;

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

    @GetMapping
    @Operation(summary = "Получить информацию о текущем пользователе")
    public ResponseEntity<?> getCurrentAgent() {
        String agentId = getAuthUserId();
        if (agentId != null) {
            AgentResponseDTO agent = managementService.findAgentDTOById(agentId);
            return ResponseEntity.ok().body(agent);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PutMapping
    @Operation(summary = "Обновить информацию о текущем пользователе")
    ResponseEntity<?> updateAgent(@Valid @RequestBody AgentRequestDTO newAgent) {
        String agentId = getAuthUserId();
        managementService.updateAgent(agentId, newAgent);

        EntityModel<AgentRequestDTO> entityModel = agentModelAssembler.toModel(newAgent);
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping
    @Operation(summary = "Удалить текущего пользователя")
    public ResponseEntity<?> deleteAgent() {
        String agentId = getAuthUserId();
        managementService.deleteTherapist(agentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointments/{appointmentId}")
    @Operation(summary = "Получить запись клиента по идентификатору")
    public EntityModel<Appointment> getAppointmentById(@PathVariable String appointmentId) {
        String agentId = getAuthUserId();
        Appointment appointment = managementService.findAppointmentById(agentId, appointmentId);
        return EntityModel.of(appointment);
    }

    @GetMapping("/appointments")
    @Operation(summary = "Получить все записи", description = "Отображает список всех записей текущего пользователя")
    public CollectionModel<EntityModel<Appointment>> allAppointments() {
        String agentId = getAuthUserId();
        return appointmentModelAssembler.toCollectionModel(managementService.findAllAppointments(agentId));
    }

    @DeleteMapping("/appointments/{appointmentId}")
    @Operation(summary = "Удалить запись по идентификатору")
    public ResponseEntity<?> deleteAppointment(@PathVariable String appointmentId) {
        String agentId = getAuthUserId();
        managementService.deleteAppointment(agentId, appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availabilities")
    @Operation(summary = "Доступное время", description = "Получить все доступное для записи время")
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
    @Operation(summary = "Добавить доступное для записи время")
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
    @Operation(summary = "Удалить доступное для записи время", description = "Удалить время за определенную дату")
    public ResponseEntity<?> deleteAppointment(@RequestParam LocalDate date) {
        String agentId = getAuthUserId();
        managementService.deleteAvailableTime(agentId, date);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services")
    @Operation(summary = "Получить доступные для записи услуги текущего пользователя")
    public EntityModel<AgentService> getService() {
        String agentId = getAuthUserId();
        AgentService service = managementService.findService(agentId);
        return EntityModel.of(service, linkTo(methodOn(ManagementController.class)
                .getCurrentAgent()).withRel("agent"));
    }

    @PostMapping("/services")
    @Operation(summary = "Добавить доступную для записи услугу")
    public EntityModel<AgentService> newService(@Valid @RequestBody AgentServiceDTO service) {
        String agentId = getAuthUserId();
        AgentService savedService = managementService.addOrUpdateService(agentId, service);
        return EntityModel.of(savedService, linkTo(methodOn(ManagementController.class)
                .getCurrentAgent()).withRel("agent"));
    }

    @GetMapping("/clients")
    @Operation(summary = "Получить список всех клиентов записавшихся на услуги текущего пользователя")
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
