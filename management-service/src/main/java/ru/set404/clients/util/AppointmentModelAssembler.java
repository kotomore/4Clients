package ru.set404.clients.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.set404.clients.controllers.TherapistController;
import ru.set404.clients.dto.AppointmentDTO;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AppointmentModelAssembler implements RepresentationModelAssembler<AppointmentDTO, EntityModel<AppointmentDTO>> {
    @Override
    public EntityModel<AppointmentDTO> toModel(AppointmentDTO entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(TherapistController.class)
                        .getAppointmentById(entity.getAppointmentId()))
                        .withSelfRel(),
                linkTo(methodOn(TherapistController.class)
                        .allAppointments())
                        .withRel("appointments"));
    }

    @Override
    public CollectionModel<EntityModel<AppointmentDTO>> toCollectionModel(Iterable<? extends AppointmentDTO> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
