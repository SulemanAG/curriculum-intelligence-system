package org.curriculumai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI curriculumAiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Curriculum Intelligence System (Curriculum AI) API")
                        .description("Multi-Agent Coordinator & Specialist Services for Continuous Curriculum Intelligence")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Curriculum AI Team")
                                .email("curriculum-ai@internal.kletech.ac.in")));
    }
}

