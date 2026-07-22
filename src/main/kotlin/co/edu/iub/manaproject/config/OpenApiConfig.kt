package co.edu.iub.manaproject.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val bearerSchemeName = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("MANA FOOD API")
                    .version("1.0.0")
                    .description("API REST para gestión de menú, pedidos y usuarios")
            )
            .addSecurityItem(SecurityRequirement().addList(bearerSchemeName))
            .components(
                Components().addSecuritySchemes(
                    bearerSchemeName,
                    SecurityScheme()
                        .name(bearerSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
    }
}