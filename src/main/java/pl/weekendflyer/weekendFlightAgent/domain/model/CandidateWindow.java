package pl.weekendflyer.weekendFlightAgent.domain.model;

import pl.weekendflyer.weekendFlightAgent.domain.planner.WindowKeyGenerator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record CandidateWindow(
        String origin,
        String destination,
        LocalDate departDate,
        LocalDate returnDate
) {

    public CandidateWindow {
        if (origin == null || destination == null || departDate == null || returnDate == null) {
            throw new IllegalArgumentException("All parameters must be non-null");
        }
        if (!returnDate.isAfter(departDate)) {
            throw new IllegalArgumentException("returnDate must be after departDate");
        }
        int days = (int) ChronoUnit.DAYS.between(departDate, returnDate) - 1;
        if (days < 1) {
            throw new IllegalArgumentException("fullDays must be at least 1");
        }
    }

    public int fullDays() {
        return (int) ChronoUnit.DAYS.between(departDate, returnDate) - 1;
    }

    public String windowKey() {
        return WindowKeyGenerator.generate(origin, destination, departDate, returnDate);
    }

    public boolean hasSaturdayInMiddle() {
        LocalDate day = departDate.plusDays(1);
        while (day.isBefore(returnDate)) {
            if (day.getDayOfWeek() == DayOfWeek.SATURDAY) {
                return true;
            }
            day = day.plusDays(1);
        }
        return false;
    }
}

