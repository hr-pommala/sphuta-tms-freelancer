package net.sphuta.tms.freelancer.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j; // ✅ Lombok annotation to inject SLF4J logger
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ----------------------------------------------------------------------------
 *  TmsApiConfig
 * ----------------------------------------------------------------------------
 *  Purpose:
 *    Centralized configuration for Springdoc OpenAPI / Swagger UI.
 *
 *  Notes:
 *    - This class only defines documentation metadata (title, version, etc.).
 *    - It does NOT affect runtime behavior of your controllers/endpoints.
 *    - Swagger UI endpoint is configured via application.yml (springdoc.*) and
 *      is typically available at: /swagger-ui.html
 *
 *  Logging:
 *    Emits INFO log when building the OpenAPI bean and DEBUG with the
 *    configured title/version to aid diagnostics without changing behavior.
 * ----------------------------------------------------------------------------
 */
@Slf4j // ✅ Lombok will auto-generate a static final Logger named "log"
@Configuration
public class TmsFreelancerApiConfig {

    /**
     * Creates and registers the OpenAPI bean used by springdoc to render
     * Swagger UI and the /api-docs JSON.
     *
     * @return OpenAPI instance containing API metadata and external docs link.
     */
    @Bean
    public OpenAPI tmsOpenApi() {
        // block-level: start building base API metadata
        final String apiTitle = "Sphuta TMS - Freelancer API";
        final String apiVersion = "v1";
        final String apiDescription = "Timesheets and Time Entries API with approvals and locking";

        if (log.isInfoEnabled()) {
            log.info("Initializing OpenAPI bean for Swagger UI");
        }

        // construct OpenAPI info with contact details
        Info info = new Info()
                .title(apiTitle)
                .version(apiVersion)
                .description(apiDescription)
                .contact(new Contact()
                        .name("Sphuta")
                        .email("support@sphuta.net"));

        // optional external documentation reference
        ExternalDocumentation externalDocs = new ExternalDocumentation()
                .description("Docs")
                .url("https://example.com/docs");

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .externalDocs(externalDocs);

        if (log.isDebugEnabled()) {
            log.debug("OpenAPI configured with title='{}', version='{}'", apiTitle, apiVersion);
        }

        return openAPI;
    }
}
