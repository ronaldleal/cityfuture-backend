package com.cityfuture.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CityFuture Backend API")
                        .description("Sistema de gestión de construcciones para la ciudadela del futuro. " +
                                "Permite gestionar órdenes de construcción, materiales, y programar construcciones secuenciales con validaciones automáticas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo CityFuture")
                                .email("cityfuture@example.com")
                                .url("https://github.com/cityfuture"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de desarrollo local"),
                        new Server()
                                .url("https://api.cityfuture.com")
                                .description("Servidor de producción")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Introduce el token JWT obtenido del endpoint de login")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearer-jwt"));
    }
}