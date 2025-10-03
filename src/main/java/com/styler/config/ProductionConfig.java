package com.styler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@Profile("prod")
public class ProductionConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductionConfig.class);
    
    @Bean
    public String productionReadyIndicator() {
        logger.info("Production configuration loaded successfully");
        logger.info("Environment variables:");
        logger.info("PORT: {}", System.getenv("PORT"));
        logger.info("DATABASE_URL present: {}", System.getenv("DATABASE_URL") != null);
        logger.info("PGUSER present: {}", System.getenv("PGUSER") != null);
        
        return "production-ready";
    }
}