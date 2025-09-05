package net.sphuta.tms.freelancer.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

/**
 * ==========================================================
 * {@code TmsFreelancerApiConfig}
 * ==========================================================
 *
 * <p>Configuration class for the Freelancer API module.
 * This class centralizes:</p>
 * <ul>
 *   <li>Swagger/OpenAPI setup for API documentation and UI.</li>
 *   <li>Jackson {@link ObjectMapper} bean customization with Java 8+ Date/Time support.</li>
 *   <li>Global CORS (Cross-Origin Resource Sharing) configuration.</li>
 * </ul>
 *
 * <p>Uses {@link Slf4j} logging to provide feedback on configuration initialization.</p>
 *
 * Annotations:
 * <ul>
 *   <li>{@link Configuration} → Marks this as a Spring configuration class.</li>
 *   <li>{@link OpenAPIDefinition} → Declares high-level API metadata for Swagger UI.</li>
 *   <li>{@link Slf4j} → Provides a logger for structured logging.</li>
 * </ul>
 */
@Slf4j
@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(
                title = "Sphuta TMS Freelancer API",
                version = "v1",
                description = "Clients, Time-Entries, Invoices, Estimates"
        )
)
public class TmsFreelancerApiConfig {

    /**
     * ----------------------------------------------------------
     * OpenAPI Bean
     * ----------------------------------------------------------
     *
     * <p>Defines and registers the {@link OpenAPI} bean used by
     * Springdoc to generate Swagger UI and API documentation.</p>
     *
     * <p>Includes metadata like API title, version, description,
     * and server details. External documentation links can also
     * be provided here.</p>
     *
     * @return a fully configured {@link OpenAPI} object
     */
    @Bean
    public OpenAPI openAPI() {
        final String apiTitle = "Sphuta TMS - Freelancer API";
        final String apiVersion = "v1";
        final String apiDescription = "Timesheets and Time Entries API with approvals and locking";

        log.info("Initializing OpenAPI bean for Swagger UI");

        // Build API metadata
        Info info = new Info()
                .title(apiTitle)
                .version(apiVersion)
                .description(apiDescription)
                .contact(new Contact()
                        .name("Sphuta")
                        .email("support@sphuta.net")
                );

        // External documentation link
        ExternalDocumentation externalDocs = new ExternalDocumentation()
                .description("Docs")
                .url("https://example.com/docs");

        // List of supported servers
        List<Server> servers = List.of(
                new Server().url("http://localhost:8080").description("Local")
        );

        OpenAPI openAPI = new OpenAPI()
                .info(info)
                .externalDocs(externalDocs)
                .servers(servers);

        log.debug("OpenAPI configured with title='{}', version='{}', servers={}",
                info.getTitle(), info.getVersion(),
                servers.stream().map(Server::getUrl).toList());

        return openAPI;
    }

    /**
     * ----------------------------------------------------------
     * ObjectMapper Bean
     * ----------------------------------------------------------
     *
     * <p>Provides a customized {@link ObjectMapper} for
     * serializing and deserializing JSON. Registers
     * {@link JavaTimeModule} to handle Java 8+ date/time
     * (e.g., {@code OffsetDateTime}, {@code LocalDateTime}).</p>
     *
     * @param builder Spring's {@link Jackson2ObjectMapperBuilder}
     * @return an {@link ObjectMapper} with Java time support
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        log.debug("Building ObjectMapper with JavaTimeModule");
        return builder.createXmlMapper(false) // disables XML mapping
                .modulesToInstall(new JavaTimeModule())
                .build();
    }

    /**
     * ----------------------------------------------------------
     * CORS Configurer Bean
     * ----------------------------------------------------------
     *
     * <p>Configures global CORS (Cross-Origin Resource Sharing)
     * settings. In this demo, all origins, headers, and methods
     * are allowed.</p>
     *
     * <p><b>WARNING:</b> For production, restrict origins and
     * methods to trusted sources only.</p>
     *
     * @return a {@link WebMvcConfigurer} instance with CORS rules
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        log.warn("CORS: allowing all origins for demo purposes");
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // applies to all endpoints
                        .allowedOrigins("*") // allow all origins
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");
            }
        };
    }
}
