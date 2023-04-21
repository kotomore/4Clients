package ru.set404.clients.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

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
