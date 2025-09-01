package net.sphuta.tms.freelancer;  // Package declaration, defines the namespace for this application

// Importing Spring Boot classes for application startup
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Lombok annotation for logging support
import lombok.extern.slf4j.Slf4j;

/**
 * TimesheetApplication
 * ------------------------------------------------------
 * This is the main entry point for the Sphuta TMS Freelancer API.
 * It bootstraps the Spring Boot application and initializes
 * all the required beans, configurations, and auto-configured
 * components.
 *
 * <p>
 * The @SpringBootApplication annotation combines:
 * - @Configuration: Marks the class as a source of bean definitions
 * - @EnableAutoConfiguration: Enables Spring Boot auto-configuration
 * - @ComponentScan: Scans this package and sub-packages for beans/components
 * </p>
 *
 * The @Slf4j annotation from Lombok generates a logger instance
 * that can be used for structured logging.
 */
@Slf4j
@SpringBootApplication
public class TmsTimesheetApplication {   // Main application class

	/**
	 * The main method that starts the Spring Boot application.
	 *
	 * @param args Command line arguments passed during application startup
	 *
	 * <p>
	 * - SpringApplication.run() → Boots the application
	 *   1. Creates an ApplicationContext (IoC container)
	 *   2. Starts auto-configuration (e.g., web server, data sources)
	 *   3. Triggers scanning of annotated classes
	 *   4. Launches the application runtime
	 * </p>
	 */
	public static void main(String[] args) {
		// Start the Spring Boot application with the given configuration class
		SpringApplication.run(TmsTimesheetApplication.class, args);

		// Example of logging after startup (INFO level)
		log.info("✅ TMS Timesheet Application has started successfully!");
	}
}
