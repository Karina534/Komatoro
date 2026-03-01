package org.example.komatoro.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурационный класс для настройки Swagger документации.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Komatoro API",
                version = "1.0",
                description = "API для управления задачами и помодоро сессиями",
                contact = @Contact(
                        name = "Smirnova Karina",
                        email = "smirnova7.04@mail.ru"
                )
        )
)
public class SwaggerConfig {

}
