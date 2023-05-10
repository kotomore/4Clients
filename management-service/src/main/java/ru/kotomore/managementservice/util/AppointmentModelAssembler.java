package ru.kotomore.managementservice.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import ru.kotomore.managementservice.controllers.ManagementController;
import ru.kotomore.managementservice.models.Appointment;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AppointmentModelAssembler implements RepresentationModelAssembler<Appointment, EntityModel<Appointment>> {
    @Override
    public EntityModel<Appointment> toModel(Appointment entity) {
        return EntityModel.of(entity,
                WebMvcLinkBuilder.linkTo(methodOn(ManagementController.class)
                        .getAppointmentById(entity.getId()))
                        .withSelfRel(),
                linkTo(methodOn(ManagementController.class)
                        .allAppointments())
                        .withRel("appointments"));
    }

    @Override
    public CollectionModel<EntityModel<Appointment>> toCollectionModel(Iterable<? extends Appointment> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
