package com.styler.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod") // Only apply this configuration for the 'prod' profile
public class DataSourceConfig {

    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Bean
    public DataSource dataSource() {
        String correctedUrl = databaseUrl;
        // The JDBC driver needs "jdbc:postgresql://" but Render provides "postgres://"
        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            correctedUrl = "jdbc:" + databaseUrl;
        }

        return DataSourceBuilder.create()
                .url(correctedUrl)
                .build();
    }
}
