package ru.set404.telegramservice.repositories;

import ru.set404.telegramservice.models.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Integer> {
    Optional<TelegramUser> findByPhone(String phone);
}
