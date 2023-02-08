package ru.set404.clients.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import ru.set404.clients.exceptions.ClientNotFoundException;
import ru.set404.clients.models.Client;
import ru.set404.clients.repositories.ClientRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class ClientController {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @GetMapping("/clients")
    public CollectionModel<EntityModel<Client>> all() {
        List<EntityModel<Client>> clients = clientRepository.findAll().stream()
                .map(client -> EntityModel.of(client,
                        linkTo(methodOn(ClientController.class).one(client.getId())).withSelfRel(),
                        linkTo(methodOn(ClientController.class).all()).withRel("clients")))
                .collect(Collectors.toList());

        return CollectionModel.of(clients, linkTo(methodOn(ClientController.class).all()).withSelfRel());
    }

    @PostMapping("/clients")
    public Client newClient(@RequestBody Client newClient) {
        return clientRepository.save(newClient);
    }

    @GetMapping("/clients/{id}")
    public EntityModel<Client> one(@PathVariable Long id) {

        Client client = clientRepository.findById(id) //
                .orElseThrow(() -> new ClientNotFoundException(id));

        return EntityModel.of(client, //
                linkTo(methodOn(ClientController.class).one(id)).withSelfRel(),
                linkTo(methodOn(ClientController.class).all()).withRel("clients"));
    }

    @PutMapping("/clients/{id}")
    Client replaceEmployee(@RequestBody Client newClient, @PathVariable Long id) {
        return clientRepository.findById(id)
                .map(client -> {
                    client.setFirstName(newClient.getFirstName());
                    client.setLastName(newClient.getLastName());
                    client.setEmail(newClient.getEmail());
                    client.setPhone(newClient.getPhone());
                    client.setRole(newClient.getRole());
                    return clientRepository.save(client);
                })
                .orElseGet(() -> {
                    newClient.setId(id);
                    return clientRepository.save(newClient);
                });
    }

    @DeleteMapping("/client/{id}")
    void deleteEmployee(@PathVariable Long id) {
        clientRepository.deleteById(id);
    }
}
