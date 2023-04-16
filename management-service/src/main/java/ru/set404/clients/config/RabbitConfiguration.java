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

    @Value("${spring.rabbitmq.host}")
    private String rabbitHostname;
    @Value("${spring.rabbitmq.username}")
    private String rabbitUser;
    @Value("${spring.rabbitmq.password}")
    private String rabitPassword;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory =
                new CachingConnectionFactory(rabbitHostname);

        connectionFactory.setUsername(rabbitUser);
        connectionFactory.setPassword(rabitPassword);
        return connectionFactory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonMessageConverter();
    }

    @Bean
    public RabbitTemplate jsonRabbitTemplate() {
        final var jsonRabbitTemplate = new RabbitTemplate(connectionFactory());
        jsonRabbitTemplate.setMessageConverter(messageConverter());
        return jsonRabbitTemplate;
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
