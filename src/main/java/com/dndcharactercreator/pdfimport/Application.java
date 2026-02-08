package com.dndcharactercreator.pdfimport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point and bootstrap class for the D&D Character Creator application.
 *
 * <p>This class is responsible for starting the Spring Boot application,
 * initializing the application context, performing component scanning,
 * and launching the embedded web server.
 *
 * <p>The {@code @SpringBootApplication} annotation enables auto-configuration
 * and scans this package and all subpackages for Spring-managed components
 * such as controllers, services, and repositories.
 *
 * <p>This class contains no business logic and should rarely require
 * modification once configured.
 *
 * @author Carter Ballard
 */
@SpringBootApplication
public class Application {

    /**
     * Main entry point for the application.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
