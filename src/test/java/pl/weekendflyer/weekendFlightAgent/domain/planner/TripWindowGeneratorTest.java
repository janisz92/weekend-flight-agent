package pl.weekendflyer.weekendFlightAgent.domain.planner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.weekendflyer.weekendFlightAgent.domain.model.CandidateWindow;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TripWindowGeneratorTest {

    private static final ZoneId WARSAW_ZONE = ZoneId.of("Europe/Warsaw");

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        LocalDate wednesday = LocalDate.of(2026, 1, 14);
        fixedClock = Clock.fixed(
                wednesday.atStartOfDay(WARSAW_ZONE).toInstant(),
                WARSAW_ZONE
        );
    }

    @Test
    void shouldGenerateOnlyWindowsWithSaturdayInMiddle() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                14,
                List.of(1, 2, 3)
        );

        assertTrue(windows.stream().allMatch(CandidateWindow::hasSaturdayInMiddle));
    }

    @Test
    void shouldNotGenerateWindowWhenSaturdayIsDepartDate() {
        LocalDate saturday = LocalDate.of(2026, 1, 17);
        Clock saturdayClock = Clock.fixed(
                saturday.atStartOfDay(WARSAW_ZONE).toInstant(),
                WARSAW_ZONE
        );
        TripWindowGenerator generator = new TripWindowGenerator(saturdayClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                7,
                List.of(2)
        );

        boolean hasWindowWithSaturdayDepart = windows.stream()
                .anyMatch(w -> w.departDate().equals(saturday));

        assertFalse(hasWindowWithSaturdayDepart);
    }

    @Test
    void shouldNotGenerateWindowWhenSaturdayIsReturnDate() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                14,
                List.of(1, 2, 3, 4, 5)
        );

        LocalDate saturday = LocalDate.of(2026, 1, 17);
        boolean hasWindowWithSaturdayReturn = windows.stream()
                .anyMatch(w -> w.returnDate().equals(saturday));

        assertFalse(hasWindowWithSaturdayReturn);
    }

    @Test
    void shouldGenerateWindowsForAllFullDaysAllowed() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                30,
                List.of(2, 3, 4)
        );

        List<Integer> fullDaysValues = windows.stream()
                .map(CandidateWindow::fullDays)
                .distinct()
                .sorted()
                .toList();

        assertTrue(fullDaysValues.contains(2) || fullDaysValues.contains(3) || fullDaysValues.contains(4));
    }

    @Test
    void shouldCalculateFullDaysCorrectlyFromDates() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                30,
                List.of(2)
        );

        for (CandidateWindow window : windows) {
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    window.departDate(), window.returnDate());
            assertEquals(daysBetween - 1, window.fullDays());
            assertEquals(2, window.fullDays());
        }
    }

    @Test
    void shouldRespectMaxWindowsPerDestinationPerDepartDate() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 2, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                30,
                List.of(1, 2, 3, 4, 5)
        );

        Map<String, Long> countPerDestDepartDate = windows.stream()
                .collect(Collectors.groupingBy(
                        w -> w.destination() + "-" + w.departDate(),
                        Collectors.counting()
                ));

        assertTrue(countPerDestDepartDate.values().stream().allMatch(count -> count <= 2));
    }

    @Test
    void shouldRespectMaxWindowsGlobal() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 5);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW", "KRK"),
                List.of("LIS", "BCN", "MAD"),
                30,
                List.of(1, 2, 3, 4)
        );

        assertEquals(5, windows.size());
    }

    @Test
    void shouldGenerateOnlyWithinHorizon() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);
        int horizonDays = 14;
        LocalDate today = LocalDate.of(2026, 1, 14);
        LocalDate horizonEnd = today.plusDays(horizonDays);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                horizonDays,
                List.of(1, 2, 3, 4)
        );

        for (CandidateWindow window : windows) {
            assertTrue(window.departDate().isBefore(horizonEnd) || window.departDate().equals(horizonEnd.minusDays(1)));
            assertTrue(window.returnDate().isBefore(horizonEnd) || window.returnDate().equals(horizonEnd));
        }
    }

    @Test
    void shouldNotGenerateWindowsWithReturnDateBeyondHorizon() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);
        int horizonDays = 7;
        LocalDate today = LocalDate.of(2026, 1, 14);
        LocalDate horizonEnd = today.plusDays(horizonDays);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                horizonDays,
                List.of(1, 2, 3)
        );

        assertTrue(windows.stream().noneMatch(w -> w.returnDate().isAfter(horizonEnd)));
    }

    @Test
    void shouldGenerateDeterministicallySortedResult() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);

        List<CandidateWindow> windows1 = generator.generate(
                List.of("WAW", "KRK"),
                List.of("LIS", "BCN"),
                14,
                List.of(1, 2, 3)
        );

        List<CandidateWindow> windows2 = generator.generate(
                List.of("WAW", "KRK"),
                List.of("LIS", "BCN"),
                14,
                List.of(1, 2, 3)
        );

        assertEquals(windows1.size(), windows2.size());
        for (int i = 0; i < windows1.size(); i++) {
            assertEquals(windows1.get(i).windowKey(), windows2.get(i).windowKey());
        }
    }

    @Test
    void shouldNotDuplicateWindows() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                30,
                List.of(1, 2, 3, 4)
        );

        List<String> windowKeys = windows.stream()
                .map(CandidateWindow::windowKey)
                .toList();

        assertEquals(windowKeys.size(), windowKeys.stream().distinct().count());
    }

    @Test
    void shouldGenerateWindowForFridayDepartSundayReturn() {
        LocalDate friday = LocalDate.of(2026, 1, 16);
        Clock fridayClock = Clock.fixed(
                friday.atStartOfDay(WARSAW_ZONE).toInstant(),
                WARSAW_ZONE
        );
        TripWindowGenerator generator = new TripWindowGenerator(fridayClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                7,
                List.of(1)
        );

        boolean hasFridayToSunday = windows.stream()
                .anyMatch(w -> w.departDate().equals(friday)
                        && w.returnDate().equals(LocalDate.of(2026, 1, 18)));

        assertTrue(hasFridayToSunday);
    }

    @Test
    void shouldReturnEmptyListWhenNoValidWindows() {
        LocalDate monday = LocalDate.of(2026, 1, 19);
        Clock mondayClock = Clock.fixed(
                monday.atStartOfDay(WARSAW_ZONE).toInstant(),
                WARSAW_ZONE
        );
        TripWindowGenerator generator = new TripWindowGenerator(mondayClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW"),
                List.of("LIS"),
                3,
                List.of(1)
        );

        assertTrue(windows.isEmpty());
    }

    @Test
    void shouldHandleMultipleOriginsAndDestinations() {
        TripWindowGenerator generator = new TripWindowGenerator(fixedClock, 100, 1000);

        List<CandidateWindow> windows = generator.generate(
                List.of("WAW", "KRK"),
                List.of("LIS", "BCN"),
                14,
                List.of(1, 2)
        );

        long wawCount = windows.stream().filter(w -> w.origin().equals("WAW")).count();
        long krkCount = windows.stream().filter(w -> w.origin().equals("KRK")).count();
        long lisCount = windows.stream().filter(w -> w.destination().equals("LIS")).count();
        long bcnCount = windows.stream().filter(w -> w.destination().equals("BCN")).count();

        assertTrue(wawCount > 0);
        assertTrue(krkCount > 0);
        assertTrue(lisCount > 0);
        assertTrue(bcnCount > 0);
    }
}

