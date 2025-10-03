package com.styler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@CrossOrigin(origins = "*")
public class StylerBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(StylerBackendApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(StylerBackendApplication.class, args);
            logger.info("Styler Backend Application started successfully");
        } catch (Exception e) {
            logger.error("Failed to start Styler Backend Application", e);
            throw e;
        }
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
            logger.info("Styler Backend Application initialized and ready to serve requests");
            logger.info("Health check endpoint available at: /api/health");
        };
    }
}