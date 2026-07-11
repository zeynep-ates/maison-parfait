package com.zeynepates.maisonparfait.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Needed by identity.RateLimiter's window-eviction sweep; will also serve
// future modules with similar cleanup jobs (e.g. inventory's stock
// reservation sweep, per docs/backend-architecture.md).
@EnableScheduling
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
