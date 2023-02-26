package ru.set404.clients.config;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class SwaggerConfig {
    static {
        var schema = new Schema<LocalTime>();
        schema.example(LocalTime.of(0,0).format(DateTimeFormatter.ofPattern("HH:mm")));
        SpringDocUtils.getConfig().replaceWithSchema(LocalTime.class, schema);

        var schema2 = new Schema<Timestamp>();
        schema2.example(LocalDateTime.of(2023,1,1,0,0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        SpringDocUtils.getConfig().replaceWithSchema(Timestamp.class, schema2);
    }
}
