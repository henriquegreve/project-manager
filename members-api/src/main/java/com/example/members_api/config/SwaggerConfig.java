package com.example.members_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI membersOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Members API (Mock Externa)")
                        .version("1.0.0")
                        .description("API REST externa mockada para criação e consulta de membros (nome e role).")
                        .contact(new Contact()
                                .name("Projeto Manager")
                                .email("contato@example.com")
                        )
                );
    }

    @Bean
    public GroupedOpenApi membersGroup() {
        return GroupedOpenApi.builder()
                .group("members-api")
                .pathsToMatch("/api/members/**")
                .build();
    }
}
