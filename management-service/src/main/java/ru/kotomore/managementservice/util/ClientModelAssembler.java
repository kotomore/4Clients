package ru.kotomore.managementservice.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.kotomore.managementservice.models.Client;

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
