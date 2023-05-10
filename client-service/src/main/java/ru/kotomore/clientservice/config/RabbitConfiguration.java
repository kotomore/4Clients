package ru.kotomore.clientservice.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    @Bean
    public TopicExchange telegramExchange() {
        return ExchangeBuilder.topicExchange("telegram_exchange").durable(true).build();
    }
}
