package pl.weekendflyer.weekendFlightAgent.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CandidateWindowTest {

    @Test
    void fullDaysShouldBeCalculatedCorrectly() {
        // departDate: 2026-01-16 (piątek), returnDate: 2026-01-18 (niedziela)
        // fullDays = 18 - 16 - 1 = 1 (sobota)
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 16),
                LocalDate.of(2026, 1, 18)
        );

        assertEquals(1, window.fullDays());
    }

    @Test
    void fullDaysShouldBe2ForFridayToMonday() {
        // departDate: 2026-01-16 (piątek), returnDate: 2026-01-19 (poniedziałek)
        // fullDays = 19 - 16 - 1 = 2 (sobota + niedziela)
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 16),
                LocalDate.of(2026, 1, 19)
        );

        assertEquals(2, window.fullDays());
    }

    @Test
    void fullDaysShouldBe3ForThursdayToMonday() {
        // departDate: 2026-01-15 (czwartek), returnDate: 2026-01-19 (poniedziałek)
        // fullDays = 19 - 15 - 1 = 3 (pt + sob + nd)
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 15),
                LocalDate.of(2026, 1, 19)
        );

        assertEquals(3, window.fullDays());
    }

    @Test
    void windowKeyShouldHaveCorrectFormat() {
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 16),
                LocalDate.of(2026, 1, 18)
        );

        assertEquals("WAW-LIS-2026-01-16-2026-01-18", window.windowKey());
    }

    @Test
    void hasSaturdayInMiddleShouldReturnTrueWhenSaturdayIsBetweenDates() {
        // departDate: 2026-01-16 (piątek), returnDate: 2026-01-18 (niedziela)
        // środek: 2026-01-17 (sobota) - TAK
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 16),
                LocalDate.of(2026, 1, 18)
        );

        assertTrue(window.hasSaturdayInMiddle());
    }

    @Test
    void hasSaturdayInMiddleShouldReturnFalseWhenSaturdayIsDepartDate() {
        // departDate: 2026-01-17 (sobota), returnDate: 2026-01-20 (wtorek)
        // środek: nd, pn - NIE MA soboty w środku
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 17),
                LocalDate.of(2026, 1, 20)
        );

        assertFalse(window.hasSaturdayInMiddle());
    }

    @Test
    void hasSaturdayInMiddleShouldReturnFalseWhenSaturdayIsReturnDate() {
        // departDate: 2026-01-14 (środa), returnDate: 2026-01-17 (sobota)
        // środek: czw, pt - NIE MA soboty w środku
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 14),
                LocalDate.of(2026, 1, 17)
        );

        assertFalse(window.hasSaturdayInMiddle());
    }

    @Test
    void hasSaturdayInMiddleShouldReturnFalseWhenNoSaturdayInRange() {
        // departDate: 2026-01-19 (niedziela), returnDate: 2026-01-23 (piątek)
        // środek: pn, wt, śr, czw - NIE MA soboty
        CandidateWindow window = new CandidateWindow(
                "WAW", "LIS",
                LocalDate.of(2026, 1, 19),
                LocalDate.of(2026, 1, 23)
        );

        assertFalse(window.hasSaturdayInMiddle());
    }

    @Test
    void shouldThrowExceptionWhenReturnDateIsNotAfterDepartDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateWindow(
                        "WAW", "LIS",
                        LocalDate.of(2026, 1, 18),
                        LocalDate.of(2026, 1, 16)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenReturnDateEqualsDepartDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateWindow(
                        "WAW", "LIS",
                        LocalDate.of(2026, 1, 16),
                        LocalDate.of(2026, 1, 16)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenFullDaysIsLessThan1() {
        // departDate: 2026-01-16, returnDate: 2026-01-17
        // fullDays = 17 - 16 - 1 = 0 (za mało)
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateWindow(
                        "WAW", "LIS",
                        LocalDate.of(2026, 1, 16),
                        LocalDate.of(2026, 1, 17)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenOriginIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateWindow(
                        null, "LIS",
                        LocalDate.of(2026, 1, 16),
                        LocalDate.of(2026, 1, 18)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDestinationIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateWindow(
                        "WAW", null,
                        LocalDate.of(2026, 1, 16),
                        LocalDate.of(2026, 1, 18)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenDepartDateIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateWindow(
                        "WAW", "LIS",
                        null,
                        LocalDate.of(2026, 1, 18)
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenReturnDateIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new CandidateWindow(
                        "WAW", "LIS",
                        LocalDate.of(2026, 1, 16),
                        null
                )
        );
    }
}

