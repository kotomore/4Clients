package ru.set404.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.set404.clients.config.RabbitConfiguration;


@SpringBootApplication
@Import(RabbitConfiguration.class)
public class ClientsAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientsAuthApplication.class, args);
    }

}
