package ru.set404.clients.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
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

        var schema3 = new Schema<LocalDateTime>();
        schema3.example(LocalDateTime.of(2023,1,1,0,0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        SpringDocUtils.getConfig().replaceWithSchema(LocalDateTime.class, schema3);
    }

    @Bean
    public OpenAPI customizeOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
