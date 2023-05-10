package ru.kotomore.authservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.MessageConverter;
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

    @Bean
    public Queue register() {
        return new Queue("register");
    }
}
