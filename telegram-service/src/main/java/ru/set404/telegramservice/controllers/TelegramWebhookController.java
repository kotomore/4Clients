package ru.set404.telegramservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.set404.telegramservice.models.Agent;
import ru.set404.telegramservice.services.RabbitService;
import ru.set404.telegramservice.telegram.WriteReadBot;

@RestController
@RequiredArgsConstructor
public class TelegramWebhookController {
    private final WriteReadBot telegramBot;
    private final RabbitService rabbitService;

    @PostMapping("/telegram-webhook")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}

