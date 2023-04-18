package ru.set404.clients.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.set404.clients.controllers.ManagementController;
import ru.set404.clients.dto.AgentDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AgentModelAssembler implements RepresentationModelAssembler<AgentDTO, EntityModel<AgentDTO>> {

    @Override
    public EntityModel<AgentDTO> toModel(AgentDTO entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(ManagementController.class)
                        .getCurrentAgent())
                        .withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<AgentDTO>> toCollectionModel(Iterable<? extends AgentDTO> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
