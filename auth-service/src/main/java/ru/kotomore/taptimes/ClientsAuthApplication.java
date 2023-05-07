package ru.kotomore.taptimes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.kotomore.taptimes.config.RabbitConfiguration;


@SpringBootApplication
@Import(RabbitConfiguration.class)
public class ClientsAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientsAuthApplication.class, args);
    }

}
