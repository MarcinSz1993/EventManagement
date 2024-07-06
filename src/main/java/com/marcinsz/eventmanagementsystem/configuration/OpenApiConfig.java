package com.marcinsz.eventmanagementsystem.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
@OpenAPIDefinition
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .contact(new Contact()
                                .name("Marcin")
                                .email("marcinsz1993@hotmail.com"))
                        .description("OpenApi documentation for EventManagementSystem")
                        .title("OpenApi specification - MarcinSz")
                        .version("1.0")
                        .license(new License()
                                .name("License name")
                                .url("https://example-url.com"))
                        .termsOfService("Terms of service"))
                .servers(List.of(new Server()
                        .description("Local ENV")
                        .url("http://localhost:8080")))
                .security(List.of(new SecurityRequirement()
                        .addList("bearerAuth")))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .name("beaterAuth")
                        .description("JWT auth description")
                        .scheme("bearer")
                        .type(SecurityScheme.Type.HTTP)
                        .bearerFormat("bearer")
                        .in(SecurityScheme.In.HEADER));
    }
}
