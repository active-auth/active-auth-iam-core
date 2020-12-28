package cn.glogs.activeauth.iamcore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class SpringDoc {
    
    private static final String API_KEY = "apiKey";

    private final cn.glogs.activeauth.iamcore.config.properties.Configuration configuration;

    public SpringDoc(cn.glogs.activeauth.iamcore.config.properties.Configuration configuration) {
        this.configuration = configuration;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(API_KEY, apiKeySecuritySchema())) // define the apiKey SecuritySchema
                .info(new Info().title("Active Auth IAM Core").description("Identity and Access Management Center of a Managed Microservice System."))
                .security(Collections.singletonList(new SecurityRequirement().addList(API_KEY))); // then apply it. If you don't apply it will not be added to the header in cURL
    }

    public SecurityScheme apiKeySecuritySchema() {
        return new SecurityScheme()
                .name(configuration.getAuthorizationHeaderName()) // authorisation-token Constants.AUTHORISATION_TOKEN
                .description("HTTP header for token on any of your request.")
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.APIKEY);
    }
}
