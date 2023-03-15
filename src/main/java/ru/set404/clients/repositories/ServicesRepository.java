package ru.set404.clients.repositories;

import ru.set404.clients.models.Service;

import java.util.Optional;

public interface ServicesRepository {

    Optional<Service> findServiceByTherapist(Long therapistId);

    void addOrUpdateService(Long therapistId, Service service);
}
