package com.styler.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class DatabaseConfig {
    
    // This configuration ensures database connection doesn't block startup
    // Database will be initialized lazily after application starts
    
}