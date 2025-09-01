package net.sphuta.tms.freelancer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * # TmsApiConfig
 *
 * Central OpenAPI configuration for the **Sphuta TMS (Freelancer)** application.
 *
 * <p>This configuration supplies:</p>
 * <ul>
 *   <li>API metadata (title, version, description)</li>
 *   <li>Server list (useful for Swagger UI “Servers” dropdown)</li>
 * </ul>
 *
 * <p><strong>Notes</strong>:</p>
 * <ul>
 *   <li>No security schemes are defined here, per your requirement.</li>
 *   <li>We keep imports exactly as provided (e.g., {@code GroupedOpenApi}) to avoid modifying your file’s structure.</li>
 *   <li>Added lightweight SLF4J logs to help trace bean creation at startup.</li>
 * </ul>
 */
@Configuration
@Slf4j
public class TmsFreelancerApiConfig {

    /**
     * Creates and exposes the primary {@link OpenAPI} bean used by springdoc to render Swagger UI.
     *
     * <p>Metadata is intentionally minimal and environment-agnostic. If you deploy to multiple environments,
     * you can externalize server URL via properties and still keep this method unchanged.</p>
     *
     * @return a fully configured {@link OpenAPI} instance used by SpringDoc.
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
}
