package net.sphuta.tms.freelancer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * ==========================================================
 * TmsFreelancerApiConfig
 * ==========================================================
 *
 * Central OpenAPI + ObjectMapper + CORS configuration
 * for the **Sphuta TMS (Freelancer)** application.
 */
@Slf4j
@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info( // fully qualified to avoid clash
                title = "Sphuta TMS Freelancer API",
                version = "v1",
                description = "Clients, Time-Entries, Invoices, Estimates"
        )
)
public class TmsFreelancerApiConfig {

    /**
     * Creates and exposes the primary {@link OpenAPI} bean used by springdoc to render Swagger UI.
     */
    @Bean
    public OpenAPI baseOpenAPI() {
        log.info("Initializing OpenAPI bean for Sphuta TMS (Freelancer)…");

        Info info = new Info()
                .title("Sphuta TMS (Freelancer)")
                .version("1.0.0")
                .description("Projects & Clients APIs for Freelancer edition");

        List<Server> servers = List.of(
                new Server().url("http://localhost:8080").description("Local")
        );

        OpenAPI openAPI = new OpenAPI()
                .info(info)
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
     * ⚠️ In production, lock this down by allowed origins/methods.
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
