package ru.set404.clients.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.set404.clients.models.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
