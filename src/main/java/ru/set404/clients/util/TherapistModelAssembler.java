package ru.set404.clients.util;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import ru.set404.clients.controllers.TherapistController;
import ru.set404.clients.models.Therapist;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TherapistModelAssembler implements RepresentationModelAssembler<Therapist, EntityModel<Therapist>> {

    @Override
    public EntityModel<Therapist> toModel(Therapist entity) {
        return EntityModel.of(entity,
                linkTo(methodOn(TherapistController.class)
                        .getCurrentTherapist())
                        .withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<Therapist>> toCollectionModel(Iterable<? extends Therapist> entities) {
        return RepresentationModelAssembler.super.toCollectionModel(entities);
    }
}
