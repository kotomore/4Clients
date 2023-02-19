package ru.set404.clients.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.services.TherapistService;
import ru.set404.clients.util.AppointmentModelAssembler;

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

    @PostMapping("/client/appointment/create")
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
