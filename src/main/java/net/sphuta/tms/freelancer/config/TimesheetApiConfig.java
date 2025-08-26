package net.sphuta.tms.freelancer.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory; // element-level: SLF4J logging API
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
@Configuration
public class TimesheetApiConfig {

    /** Logger for this configuration (does not change functionality). */
    private static final Logger log = LoggerFactory.getLogger(TimesheetApiConfig.class);

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

        // block-level: construct OpenAPI info with contact details
        Info info = new Info()
                .title(apiTitle)
                .version(apiVersion)
                .description(apiDescription)
                .contact(new Contact()
                        .name("Sphuta")
                        .email("support@sphuta.net"));

        // block-level: optional external documentation reference
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
