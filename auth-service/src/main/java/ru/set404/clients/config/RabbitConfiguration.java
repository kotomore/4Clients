package ru.set404.clients.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    @Bean
    public MessageConverter messageConverter() {
        return new JacksonMessageConverter();
    }

    @Bean
    public Queue login() {
        return new Queue("login");
    }

    @Bean
    public Queue refresh() {
        return new Queue("refresh");
    }

    @Bean
    public Queue access() {
        return new Queue("access");
    }
}
