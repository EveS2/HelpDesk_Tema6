package br.edu.helpdesk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {
    @Bean OpenAPI documentacao() {
        String nome = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Help Desk - Tema 6").version("1.0")
                        .description("API de chamados da rede de escolas. Login: /api/auth/login"))
                .components(new Components().addSecuritySchemes(nome,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP)
                                .scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(nome));
    }
}
