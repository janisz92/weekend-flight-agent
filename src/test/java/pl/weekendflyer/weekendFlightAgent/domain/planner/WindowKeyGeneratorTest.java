package pl.weekendflyer.weekendFlightAgent.domain.planner;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class WindowKeyGeneratorTest {

    @Test
    void shouldGenerateCorrectFormat() {
        String key = WindowKeyGenerator.generate(
                "WAW",
                "LIS",
                LocalDate.of(2026, 1, 16),
                LocalDate.of(2026, 1, 18)
        );

        assertEquals("WAW-LIS-2026-01-16-2026-01-18", key);
    }

    @Test
    void shouldGenerateCorrectFormatWithDifferentDates() {
        String key = WindowKeyGenerator.generate(
                "KRK",
                "BCN",
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 5)
        );

        assertEquals("KRK-BCN-2026-12-01-2026-12-05", key);
    }

    @Test
    void shouldThrowExceptionWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                WindowKeyGenerator.generate(
                        null,
                        "LIS",
                        LocalDate.of(2026, 1, 16),
                        LocalDate.of(2026, 1, 18)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDestinationIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                WindowKeyGenerator.generate(
                        "WAW",
                        null,
                        LocalDate.of(2026, 1, 16),
                        LocalDate.of(2026, 1, 18)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDepartDateIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                WindowKeyGenerator.generate(
                        "WAW",
                        "LIS",
                        null,
                        LocalDate.of(2026, 1, 18)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenReturnDateIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                WindowKeyGenerator.generate(
                        "WAW",
                        "LIS",
                        LocalDate.of(2026, 1, 16),
                        null
                )
        );
    }
}

