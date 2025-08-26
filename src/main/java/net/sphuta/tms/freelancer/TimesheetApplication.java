package net.sphuta.tms.freelancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

/**
 * TimesheetApplication
 * ------------------------------------------------------
 * This is the main entry point for the Sphuta TMS Freelancer API.
 * It bootstraps the Spring Boot application and initializes
 * all the required beans, configurations, and auto-configured
 * components.
 */
@Slf4j
@SpringBootApplication
public class TimesheetApplication {

	/**
	 * The main method that starts the Spring Boot application.
	 *
	 * @param args Command line arguments passed during application startup
	 */
	public static void main(String[] args) {

		// ---- Block level comment ----
		// Log an informational message before starting the Spring Boot app.
		log.info("Starting Sphuta TMS Freelancer API...");

		// ---- Element level comment ----
		// This method triggers the Spring Boot auto-configuration process.
		SpringApplication.run(TimesheetApplication.class, args);

		// ---- Block level comment ----
		// Log a confirmation message once the application is up.
		log.info("Sphuta TMS Freelancer API started successfully!");
	}
}
