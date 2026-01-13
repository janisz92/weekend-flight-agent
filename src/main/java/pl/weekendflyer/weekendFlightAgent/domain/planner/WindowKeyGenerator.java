package pl.weekendflyer.weekendFlightAgent.domain.planner;

import java.time.LocalDate;

public final class WindowKeyGenerator {

    private WindowKeyGenerator() {
    }

    public static String generate(String origin, String destination, LocalDate departDate, LocalDate returnDate) {
        if (origin == null || destination == null || departDate == null || returnDate == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        return String.format("%s-%s-%s-%s", origin, destination, departDate, returnDate);
    }
}

