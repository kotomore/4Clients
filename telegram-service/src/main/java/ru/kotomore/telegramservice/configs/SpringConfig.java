package ru.kotomore.telegramservice.configs;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import ru.kotomore.telegramservice.telegram.WriteReadBot;
import ru.kotomore.telegramservice.telegram.handlers.CallbackQueryHandler;
import ru.kotomore.telegramservice.telegram.handlers.TelegramMessageHandler;


@Configuration
@AllArgsConstructor
public class SpringConfig {
    private final TelegramConfig telegramConfig;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(telegramConfig.getWebhookPath()).build();
    }

    @Bean
    public WriteReadBot springWebhookBot(SetWebhook setWebhook,
                                         TelegramMessageHandler telegramMessageHandler,
                                         CallbackQueryHandler callbackQueryHandler) {
        String botToken = telegramConfig.getBotToken();

        WriteReadBot bot = new WriteReadBot(setWebhook, botToken, telegramMessageHandler, callbackQueryHandler);

        bot.setBotPath(telegramConfig.getWebhookPath());
        bot.setBotUsername(telegramConfig.getBotName());
        bot.setBotToken(telegramConfig.getBotToken());

        return bot;
    }
}
