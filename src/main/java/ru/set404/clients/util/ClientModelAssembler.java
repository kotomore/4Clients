package ru.set404.clients.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.set404.clients.controllers.TherapistController;
import ru.set404.clients.models.Appointment;
import ru.set404.clients.models.Client;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ClientModelAssembler implements RepresentationModelAssembler<Client, EntityModel<Client>> {
    @Override
    public EntityModel<Client> toModel(Client entity) {
        return EntityModel.of(entity);
    }

    @Override
    public CollectionModel<EntityModel<Client>> toCollectionModel(Iterable<? extends Client> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
