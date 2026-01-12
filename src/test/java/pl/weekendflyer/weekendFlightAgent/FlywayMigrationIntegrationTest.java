package pl.weekendflyer.weekendFlightAgent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FlywayMigrationIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationV1ShouldBeExecuted() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = true",
            Integer.class
        );

        assertNotNull(count);
        assertEquals(1, count, "Migration V1 should be executed exactly once");
    }

    @Test
    void windowCheckTableShouldExist() {
        Integer tableExists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.tables " +
            "WHERE table_schema = 'public' AND table_name = 'window_check'",
            Integer.class
        );

        assertNotNull(tableExists);
        assertEquals(1, tableExists, "Table window_check should exist");
    }

    @Test
    void allRequiredTablesShouldExist() {
        String[] expectedTables = {
            "window_check",
            "price_observation",
            "baseline",
            "deal",
            "notification_log"
        };

        for (String tableName : expectedTables) {
            Integer tableExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = 'public' AND table_name = ?",
                Integer.class,
                tableName
            );

            assertNotNull(tableExists);
            assertEquals(1, tableExists, "Table " + tableName + " should exist");
        }
    }
}

