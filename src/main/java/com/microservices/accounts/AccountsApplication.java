package com.microservices.accounts;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@OpenAPIDefinition(info = @Info(
        title = "Accounts microservice REST API Documentation",
        description = "EazyBank Accounts microservice REST API Documentation",
        version = "v1",
        contact = @Contact(
                name = "Adarsh Kumar",
                email = "demo@gmail.com",
                url = "https://adarshkumar.dev"
        ),
        license = @License(
                name = "Apache2.0",
                url = "https://adarshkumar.dev"
        )
        ), externalDocs = @ExternalDocumentation(
                description = "Accounts microservice REST API Documentation",
                url = "https://adarshkumar.dev"
        ))
public class AccountsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountsApplication.class, args);
    }

}
