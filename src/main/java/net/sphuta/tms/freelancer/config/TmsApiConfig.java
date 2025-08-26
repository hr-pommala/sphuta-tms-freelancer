package net.sphuta.tms.freelancer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * <h2>API Configuration</h2>
 * - Swagger/OpenAPI metadata
 * - Jackson (JavaTimeModule)
 * - Global CORS
 */
@Slf4j
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Sphuta TMS Freelancer API",
                version = "v1",
                description = "Clients, Time-Entries, Invoices, Estimates"))
public class TmsApiConfig {

    /** ObjectMapper with Java Time support. */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        log.debug("Building ObjectMapper with JavaTimeModule");
        return builder.createXmlMapper(false)
                .modulesToInstall(new JavaTimeModule())
                .build();
    }

    /** Open CORS for demo. Lock down by origin/methods in production. */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        log.warn("CORS: allowing all origins for demo purposes");
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS","HEAD");
            }
        };
    }
}
