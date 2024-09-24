package ru.tehnobear.essence.receiver;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "Essence Receiver Report", version = "0.0.1", description = "Микро сервис печати"),
        security = {
                @SecurityRequirement(name = "essenceCookieSession"),
                @SecurityRequirement(name = "essenceQuerySession"),
                @SecurityRequirement(name = "bearer"),
                @SecurityRequirement(name = "basic"),
                @SecurityRequirement(name = "token")
        }
)
@Configuration
public class ReceiverConfiguration {
}
