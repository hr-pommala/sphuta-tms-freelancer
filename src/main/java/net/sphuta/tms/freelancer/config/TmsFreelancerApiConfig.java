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
     * Creates and registers the OpenAPI bean used by springdoc to render Swagger UI.
     */
    @Bean
    public OpenAPI openAPI() {
        final String apiTitle = "Sphuta TMS - Freelancer API";
        final String apiVersion = "v1";
        final String apiDescription = "Timesheets and Time Entries API with approvals and locking";

        log.info("Initializing OpenAPI bean for Swagger UI");

        Info info = new Info()
                .title(apiTitle)
                .version(apiVersion)
                .description(apiDescription)
                .contact(new Contact()
                        .name("Sphuta")
                        .email("support@sphuta.net")
                );

        ExternalDocumentation externalDocs = new ExternalDocumentation()
                .description("Docs")
                .url("https://example.com/docs");

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
     * Provides ObjectMapper with Java Time support.
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        log.debug("Building ObjectMapper with JavaTimeModule");
        return builder.createXmlMapper(false)
                .modulesToInstall(new JavaTimeModule())
                .build();
    }

    /**
     * Opens CORS for demo purposes.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        log.warn("CORS: allowing all origins for demo purposes");
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD");
            }
        };
    }
}
