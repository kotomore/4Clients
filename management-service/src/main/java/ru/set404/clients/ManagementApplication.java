package ru.set404.clients;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import ru.set404.clients.config.RabbitConfiguration;


@SpringBootApplication
@Import(RabbitConfiguration.class)
public class ManagementApplication {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    public static void main(String[] args) {
        SpringApplication.run(ManagementApplication.class, args);
    }

}
