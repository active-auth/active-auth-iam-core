package cn.glogs.activeauth.iamcore.config;

import cn.glogs.activeauth.iamcore.config.properties.AuthConfiguration;
import cn.glogs.activeauth.iamcore.config.properties.MfaConfiguration;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * SpringDoc Configurations
 * https://springdoc.org/
 *
 * @author Okeyja Teung
 * @since 2021-01-11 20:08 +08:00
 */
@Configuration
public class SpringDoc {

    private static final String API_KEY = "apiKey";
    private static final String VERIFICATION_TOKEN_$REF = "mfa_token";

    private final AuthConfiguration authConfiguration;

    public SpringDoc(AuthConfiguration authConfiguration) {
        this.authConfiguration = authConfiguration;
    }

    /**
     * API KEY
     * Header:Authorization
     *
     * @return bean:SecurityScheme
     * @author Okeyja Teung
     * @since 2021-01-11 20:08 +08:00
     */
    public SecurityScheme apiKeySecuritySchema() {
        return new SecurityScheme()
                .name(authConfiguration.getAuthorizationHeaderName()) // authorisation-token Constants.AUTHORISATION_TOKEN
                .description("HTTP header for token on any of your request.")
                .in(SecurityScheme.In.HEADER)
                .type(SecurityScheme.Type.APIKEY);
    }

    /**
     * See what's different between Swagger and SpringDoc:
     * https://swagger.io/blog/api-strategy/difference-between-swagger-and-openapi/
     * https://stackoverflow.com/questions/59291371/migrating-from-springfox-swagger2-to-springdoc-openapi
     *
     * @param mfaConfiguration bean:MfaConfiguration
     * @return bean:OpenAPI
     * @author Okeyja Teung
     * @since 2021-01-11 20:08 +08:00
     */
    @Bean
    public OpenAPI customOpenAPI(MfaConfiguration mfaConfiguration) {
        Parameter parameter = new HeaderParameter().name(mfaConfiguration.getMfaTokenHeader()).in(ParameterIn.HEADER.toString()).schema(new StringSchema());
        return new OpenAPI()
                .components(new Components()
                        .addParameters(VERIFICATION_TOKEN_$REF, parameter)
                        .addSecuritySchemes(API_KEY, apiKeySecuritySchema()) // define the apiKey SecuritySchema
                )
                .info(new Info().title("Active Auth IAM Core").description("Identity and Access Management Center of a Managed Microservice System."))
                .security(Collections.singletonList(new SecurityRequirement().addList(API_KEY))); // then apply it. If you don't apply it will not be added to the header in cURL
    }

    /**
     * Add a global header customizer.
     * Or you have to add $ref on each method of your API controller.
     * <p>
     * https://github.com/springdoc/springdoc-openapi/issues/466
     * https://github.com/springdoc/springdoc-openapi/blob/master/springdoc-openapi-webmvc-core/src/test/java/test/org/springdoc/api/app39/SpringDocTestApp.java
     *
     * @return bean:OpenApiCustomiser
     * @author Okeyja Teung
     * @since 2021-01-11 20:08 +08:00
     */
    @Bean
    public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
        return openApi -> openApi.getPaths().values().stream().flatMap(pathItem -> pathItem.readOperations().stream())
                .forEach(operation -> operation.addParametersItem(new HeaderParameter().$ref(VERIFICATION_TOKEN_$REF)));
    }
}
