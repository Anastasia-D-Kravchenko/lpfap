package pl.edu.pjwstk.lpfap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lpfapOpenAPI() {
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Local Pet Foster & Adoption Portal (LPFAP) API")
                        .description("REST API for the TPO course project — animal shelter platform "
                                + "with public browsing, USER applications, and ADMIN management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PJATK · s32802")
                                .email("s32802@pjwstk.edu.pl")))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT obtained from POST /api/v1/auth/login")));
    }
}
