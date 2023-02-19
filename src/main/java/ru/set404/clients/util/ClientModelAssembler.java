//package ru.set404.clients.util;
//
//import org.springframework.hateoas.CollectionModel;
//import org.springframework.hateoas.EntityModel;
//import org.springframework.hateoas.server.RepresentationModelAssembler;
//import org.springframework.stereotype.Component;
//import ru.set404.clients.controllers.ClientController;
//import ru.set404.clients.models.Client;
//
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
//import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
//
//@Component
//public class ClientModelAssembler implements RepresentationModelAssembler<Client, EntityModel<Client>> {
//    @Override
//    public EntityModel<Client> toModel(Client entity) {
//        return EntityModel.of(entity,
//                linkTo(methodOn(ClientController.class).one(entity.getId())).withSelfRel(),
//                linkTo(methodOn(ClientController.class).all()).withRel("clients"));
//    }
//
//    @Override
//    public CollectionModel<EntityModel<Client>> toCollectionModel(Iterable<? extends Client> entities) {
//        return RepresentationModelAssembler.super.toCollectionModel(entities);
//    }
//}
