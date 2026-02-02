package com.portfoliomanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI portfolioManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portfolio Manager API")
                        .description("REST API for managing financial portfolios, assets, and market data")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Portfolio Manager Team")
                                .email("support@portfoliomanager.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server")
                ));
    }
}
