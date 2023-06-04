package ru.kotomore.managementservice.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import ru.kotomore.managementservice.controllers.ManagementController;
import ru.kotomore.managementservice.dto.AgentRequestDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AgentModelAssembler implements RepresentationModelAssembler<AgentRequestDTO, EntityModel<AgentRequestDTO>> {

    @Override
    public EntityModel<AgentRequestDTO> toModel(AgentRequestDTO entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(ManagementController.class)
                        .getCurrentAgent())
                        .withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<AgentRequestDTO>> toCollectionModel(Iterable<? extends AgentRequestDTO> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
