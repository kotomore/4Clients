package ru.kotomore.managementservice.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import ru.kotomore.managementservice.controllers.ManagementController;
import ru.kotomore.managementservice.dto.AgentDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AgentModelAssembler implements RepresentationModelAssembler<AgentDTO, EntityModel<AgentDTO>> {

    @Override
    public EntityModel<AgentDTO> toModel(AgentDTO entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(ManagementController.class)
                        .getCurrentAgent())
                        .withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<AgentDTO>> toCollectionModel(Iterable<? extends AgentDTO> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
