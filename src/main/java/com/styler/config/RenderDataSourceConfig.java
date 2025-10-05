package com.styler.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;
import java.net.URI;


@Configuration
@Profile("render-prod")
public class RenderDataSourceConfig {

    @Bean
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            try {
                URI uri = new URI(databaseUrl);
                
                // Handle port - use 5432 as default if not specified
                int port = uri.getPort();
                if (port == -1) {
                    port = 5432; // Default PostgreSQL port
                }
                
                String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
                String[] userInfo = uri.getUserInfo().split(":");
                String username = userInfo[0];
                String password = userInfo.length > 1 ? userInfo[1] : "";
                
                System.out.println("DEBUG: Original URL: " + databaseUrl);
                System.out.println("DEBUG: JDBC URL: " + jdbcUrl);
                System.out.println("DEBUG: Username: " + username);
                
                return DataSourceBuilder
                    .create()
                    .url(jdbcUrl)
                    .username(username)
                    .password(password)
                    .driverClassName("org.postgresql.Driver")
                    .build();
                    
            } catch (Exception e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
                throw new RuntimeException("Invalid DATABASE_URL format: " + databaseUrl, e);
            }
        }
        
        // Fallback to default configuration
        return DataSourceBuilder
            .create()
            .url("jdbc:postgresql://localhost:5432/styler_db")
            .username("postgres")
            .password("password")
            .driverClassName("org.postgresql.Driver")
            .build();
    }
}