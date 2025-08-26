package net.sphuta.tms.freelancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <h1>Sphuta TMS — Freelancer</h1>
 *
 * <p>Main application class for the Sphuta TMS system.
 * This is the entry point for starting the Spring Boot application.</p>
 *
 * <ul>
 *   <li>Bootstraps the Spring Application Context.</li>
 *   <li>Initializes all beans, configurations, and services.</li>
 *   <li>Acts as the starting point of execution for the TMS system.</li>
 * </ul>
 *
 * <p><b>Logging:</b> This class logs important application lifecycle events like
 * startup initiation and completion.</p>
 */
@Slf4j
@SpringBootApplication
public class TmsApplication {

	/**
	 * Main method — application entry point.
	 *
	 * @param args command-line arguments passed to the application
	 *
	 * <p><b>Execution flow:</b></p>
	 * <ol>
	 *   <li>Log that the application startup is beginning.</li>
	 *   <li>Run the Spring Boot application using {@link SpringApplication#run}.</li>
	 *   <li>Log that the application has successfully started.</li>
	 * </ol>
	 *
	 * <p><b>Logs:</b></p>
	 * <ul>
	 *   <li>INFO log before starting the Spring Boot application.</li>
	 *   <li>INFO log after the Spring Boot application is up and running.</li>
	 * </ul>
	 */
	public static void main(String[] args) {
		// Block-level comment: log before starting the application
		log.info("Starting Sphuta TMS application");

		// Bootstrapping the Spring Boot application context
		SpringApplication.run(TmsApplication.class, args);

		// Block-level comment: log after application has started successfully
		log.info("Sphuta TMS application started successfully");
	}
}
