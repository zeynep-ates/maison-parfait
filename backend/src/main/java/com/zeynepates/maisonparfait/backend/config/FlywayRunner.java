package com.zeynepates.maisonparfait.backend.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayRunner {

    @Bean
    ApplicationRunner flywayMigrateOnStartup(Flyway flyway) {
        return args -> {
            flyway.migrate();
        };
    }
}
