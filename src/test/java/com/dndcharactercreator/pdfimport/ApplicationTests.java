package com.dndcharactercreator.pdfimport;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * This is a bare {@code @SpringBootTest} with no restrictions, so it loads the entire
 * application - which now includes a real {@code DataSource} and Flyway, since JPA/Postgres
 * support was added. It needs an actual, reachable Postgres to load successfully.
 *
 * <p>Rather than requiring compose.yaml's Postgres to already be running (and colliding with it
 * on port 5432, or on whatever data happens to already be in it), Testcontainers starts a
 * throwaway Postgres in Docker just for this test class, and {@code @ServiceConnection} points
 * Spring's datasource/Flyway configuration at it automatically - no manual property wiring
 * needed.
 */
@SpringBootTest
@Testcontainers
class ApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

	@Test
	void contextLoads() {
	}

}
