package ru.kotomore.taptimes.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    @Bean
    public TopicExchange telegramExchange() {
        return ExchangeBuilder.topicExchange("telegram_exchange").durable(true).build();
    }

    @Bean
    public Queue telegramQueueFrom() {
        return QueueBuilder.durable("telegram_queue_from").build();
    }

    @Bean
    public Queue telegramUpdateService() {
        return QueueBuilder.durable("telegram_update_service").build();
    }

    @Bean
    public Queue telegramUpdateAgent() {
        return QueueBuilder.durable("telegram_update_agent").build();
    }

    @Bean
    public Queue telegramUpdateSchedule() {
        return QueueBuilder.durable("telegram_update_schedule").build();
    }

    @Bean
    public Queue telegramQueueToSchedule() {
        return QueueBuilder.durable("telegram_queue_to_schedule").build();
    }

    @Bean
    public Queue telegramQueueToAppointment() {
        return QueueBuilder.durable("telegram_queue_to_appointment").build();
    }
}
