package net.sphuta.tms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Sphuta TMS (Freelancer edition) application.
 *
 * <p>This class bootstraps the Spring Boot application. By annotating with
 * {@link SpringBootApplication}, it enables:</p>
 * <ul>
 *   <li>Component scanning (for controllers, services, repositories, etc.)</li>
 *   <li>Auto-configuration (Spring Boot configures beans based on classpath and properties)</li>
 *   <li>Configuration class declaration (this class itself may hold @Bean definitions)</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <pre>
 *   java -jar tms-freelancer.jar
 * </pre>
 *
 * <p>This will launch the embedded server (default: Tomcat on port 8080) and
 * expose REST endpoints defined under {@code /api/v1/...}.</p>
 */
@Slf4j
@SpringBootApplication
public class TmsApplication {

	/**
	 * Main method to start the Spring Boot application.
	 *
	 * @param args command-line arguments passed during startup
	 */
	public static void main(String[] args) {
		log.info("Starting Sphuta TMS (Freelancer edition)...");
		SpringApplication.run(TmsApplication.class, args);
		log.info(" Sphuta TMS started successfully. REST APIs available at http://localhost:8080/api/v1");
	}
}
