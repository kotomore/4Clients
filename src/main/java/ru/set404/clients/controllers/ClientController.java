package ru.set404.clients.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.services.TherapistService;
import ru.set404.clients.util.AppointmentModelAssembler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
public class ClientController {

    private final AppointmentModelAssembler appointmentModelAssembler;
    private final TherapistService therapistService;

    @Autowired
    public ClientController(AppointmentModelAssembler appointmentModelAssembler, TherapistService therapistService) {
        this.appointmentModelAssembler = appointmentModelAssembler;
        this.therapistService = therapistService;
    }

//    @GetMapping("/clients")
//    public CollectionModel<EntityModel<Client>> all() {
//        List<Client> clients = clientRepository.findAll();
//        return clientModelAssembler.toCollectionModel(clients);
//    }

//    @PostMapping("/clients")
//    public ResponseEntity<?> newClient(@RequestBody Client newClient) {
//        EntityModel<Client> entityModel = clientModelAssembler.toModel(clientRepository.save(newClient));
//
//        return ResponseEntity
//                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
//                .body(entityModel);
//    }

    @PostMapping("/client/appointment")
    public ResponseEntity<?> newAppointment(@RequestBody Appointment newAppointment) {

        Appointment appointment = therapistService.addAppoinment(newAppointment);

        if (appointment == null)
            return ResponseEntity
                    .status(412)
                    .body("Date is not available");

        EntityModel<Appointment> entityModel = appointmentModelAssembler.toModel(appointment);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("client/availableTimes")
    public ResponseEntity<?> availableTimes(@RequestParam Long therapistId, @RequestParam LocalDate date) {
        List<LocalTime> availableTimes = therapistService.getAvailableTimes(therapistId, date);
        if (availableTimes.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available time for appointment to date - " + date));
        return new ResponseEntity<>(availableTimes, HttpStatus.OK);
    }

    @GetMapping("client/availableDates")
    public ResponseEntity<?> availableDates(@RequestParam Long therapistId, @RequestParam LocalDate date) {
        List<LocalDate> availableDates = therapistService.getAvailableDates(therapistId, date);
        if (availableDates.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE)
                    .body(Problem.create()
                            .withTitle("Not found")
                            .withDetail("There is no available date for appointment to month - " + date.getMonth()));
        return new ResponseEntity<>(availableDates, HttpStatus.OK);
    }

//    @GetMapping("/clients/{id}")
//    public EntityModel<Client> one(@PathVariable Long id) {
//
//        Client client = clientRepository.findById(id) //
//                .orElseThrow(() -> new ClientNotFoundException(id));
//
//        return clientModelAssembler.toModel(client);
//    }
//
//    @PutMapping("/clients/{id}")
//    ResponseEntity<?> replaceEmployee(@RequestBody Client newClient, @PathVariable Long id) {
//        Client updatedClient = clientRepository.findById(id)
//                .map(client -> {
//                    client.setName(newClient.getName());
//                    client.setPhone(newClient.getPhone());
//                    return clientRepository.save(client);
//                })
//                .orElseGet(() -> {
//                    newClient.setId(id);
//                    return clientRepository.save(newClient);
//                });
//        EntityModel<Client> entityModel = clientModelAssembler.toModel(updatedClient);
//
//        return ResponseEntity
//                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
//                .body(entityModel);
//    }
//
//    @DeleteMapping("/client/{id}")
//    ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
//        clientRepository.deleteById(id);
//        return ResponseEntity.noContent().build();
//    }
}
