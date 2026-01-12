package pl.weekendflyer.weekendFlightAgent.domain.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;
import pl.weekendflyer.weekendFlightAgent.domain.model.TripConstraints;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pl.weekendflyer.weekendFlightAgent.domain.eval.FlightOfferTestHelper.*;

class TripEvaluatorSaturdayRuleTest {

    private TripEvaluator evaluator;
    private TripConstraints defaultConstraints;

    @BeforeEach
    void setUp() {
        evaluator = new TripEvaluator();
        defaultConstraints = new TripConstraints(
                1, 480, null,
                LocalTime.of(22, 0), LocalTime.of(6, 0), true
        );
    }

    @Test
    @DisplayName("Przylot pt 21:59, wylot nd 06:00, brak segmentów w sobotę => true (granica wylotu)")
    void shouldReturnTrue_whenArrivalFridayBeforeThresholdAndDepartureSundayAtThreshold() {
        // given: przylot piątek 21:59, wylot niedziela 06:00
        // Spełnia wszystkie warunki: przed progiem w piątek, w progu w niedzielę, brak lotów w sobotę
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 59, 0, 0, ROME_ZONE); // piątek 21:59
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 6, 0, 0, 0, ROME_ZONE); // niedziela 06:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 9, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertTrue(result, "Sobota powinna być uznana za pełną - przylot przed 22:00, wylot o 06:00, brak lotów w sobotę");
    }

    @Test
    @DisplayName("Przylot pt 22:00, wylot nd 06:00, brak segmentów w sobotę => true (granica przylotu włączona)")
    void shouldReturnTrue_whenArrivalFridayExactlyAtThreshold() {
        // given: przylot piątek 22:00 DOKŁADNIE - granica jest włączona (<=)
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 19, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 22, 0, 0, 0, ROME_ZONE); // piątek 22:00 DOKŁADNIE
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 6, 0, 0, 0, ROME_ZONE); // niedziela 06:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 9, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertTrue(result, "Sobota powinna być uznana za pełną - przylot dokładnie o 22:00 (granica włączona)");
    }

    @Test
    @DisplayName("Przylot pt 22:01 => false (po progu)")
    void shouldReturnFalse_whenArrivalFridayAfterThreshold() {
        // given: przylot piątek 22:01 - po progu 22:00
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 19, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 22, 1, 0, 0, ROME_ZONE); // piątek 22:01 - ZA PÓŹNO
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ROME_ZONE); // niedziela 10:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertFalse(result, "Sobota nie powinna być uznana za pełną - przylot po 22:00");
    }

    @Test
    @DisplayName("Wylot nd 05:59 => false (przed progiem)")
    void shouldReturnFalse_whenDepartureSundayBeforeThreshold() {
        // given: wylot niedziela 05:59 - przed progiem 06:00
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, ROME_ZONE); // piątek 21:00
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 5, 59, 0, 0, ROME_ZONE); // niedziela 05:59 - ZA WCZEŚNIE
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 9, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertFalse(result, "Sobota nie powinna być uznana za pełną - wylot przed 06:00");
    }

    @Test
    @DisplayName("Outbound ma przylot w sobotę (np. sob 00:10) => false")
    void shouldReturnFalse_whenOutboundArrivalOnSaturday() {
        // given: przylot w sobotę 00:10 (mimo że wylot był w piątek wieczorem)
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 22, 0, 0, 0, ROME_ZONE); // piątek 22:00
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 17, 0, 10, 0, 0, ROME_ZONE); // sobota 00:10 - LOT W SOBOTĘ
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ROME_ZONE); // niedziela 10:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertFalse(result, "Sobota nie powinna być uznana za pełną - przylot outbound w sobotę");
    }

    @Test
    @DisplayName("Inbound ma wylot w sobotę (np. sob 20:00) => false")
    void shouldReturnFalse_whenInboundDepartureOnSaturday() {
        // given: wylot powrotny w sobotę 20:00
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, ROME_ZONE); // piątek 21:00
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 17, 20, 0, 0, 0, ROME_ZONE); // sobota 20:00 - LOT W SOBOTĘ
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 17, 23, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertFalse(result, "Sobota nie powinna być uznana za pełną - wylot inbound w sobotę");
    }

    @Test
    @DisplayName("requireNoFlightOnSaturday=false: lot w sobotę dozwolony, ale progi piątek/niedziela nadal obowiązują")
    void shouldReturnTrue_whenRequireNoFlightOnSaturdayFalseAndThresholdsMetDespiteFlightOnSaturday() {
        // given: requireNoFlightOnSaturday=false - lot w sobotę jest dozwolony
        // Ale nadal sprawdzamy progi: przylot pt przed 22:00, wylot nd po 06:00
        TripConstraints relaxedConstraints = new TripConstraints(
                1,
                480,
                null,
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                false  // requireNoFlightOnSaturday = false
        );

        // Oferta z segmentem outbound, który ma przylot w sobotę (normalnie by to dyskwalifikowało)
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 23, 0, 0, 0, ROME_ZONE); // piątek 23:00
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 17, 1, 0, 0, 0, ROME_ZONE); // sobota 01:00 - ale to OK przy false
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ROME_ZONE); // niedziela 10:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, relaxedConstraints);

        // then
        // Oczekiwane zachowanie: false, bo przylot nie jest w piątek (jest w sobotę)
        // Warunek b) wymaga przylotu do destynacji w piątek przed 22:00
        assertFalse(result, "Sobota nie powinna być uznana za pełną - przylot nie jest w piątek");
    }

    @Test
    @DisplayName("requireNoFlightOnSaturday=false: przylot pt 21:00, wylot nd 10:00 => true")
    void shouldReturnTrue_whenRequireNoFlightOnSaturdayFalseAndValidFridaySundaySchedule() {
        // given: requireNoFlightOnSaturday=false, ale progi piątek/niedziela spełnione
        TripConstraints relaxedConstraints = new TripConstraints(
                1,
                480,
                null,
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                false  // requireNoFlightOnSaturday = false
        );

        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, ROME_ZONE); // piątek 21:00
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ROME_ZONE); // niedziela 10:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, relaxedConstraints);

        // then
        assertTrue(result, "Sobota powinna być uznana za pełną - progi spełnione, lot w sobotę nie wymagany");
    }

    @Test
    @DisplayName("Przylot w czwartek (nie piątek) => false")
    void shouldReturnFalse_whenArrivalNotOnFriday() {
        // given: przylot w czwartek zamiast piątku
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 15, 18, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 15, 21, 0, 0, 0, ROME_ZONE); // czwartek 21:00
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ROME_ZONE); // niedziela 10:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertFalse(result, "Sobota nie powinna być uznana za pełną - przylot nie jest w piątek");
    }

    @Test
    @DisplayName("Wylot w poniedziałek (nie niedziela) => false")
    void shouldReturnFalse_whenDepartureNotOnSunday() {
        // given: wylot powrotny w poniedziałek zamiast niedzieli
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, ROME_ZONE); // piątek 21:00
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0, ROME_ZONE); // poniedziałek 10:00
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 19, 13, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, defaultConstraints);

        // then
        assertFalse(result, "Sobota nie powinna być uznana za pełną - wylot powrotny nie jest w niedzielę");
    }

    @Test
    @DisplayName("Brak segmentów outbound => false")
    void shouldReturnFalse_whenNoOutboundSegments() {
        FlightOffer offer = buildOffer(
                List.of(),
                List.of(createSegment(
                        ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ROME_ZONE),
                        ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ROME_ZONE)
                )),
                500
        );

        assertFalse(evaluator.isSaturdayFull(offer, defaultConstraints));
    }

    @Test
    @DisplayName("Null offer => false")
    void shouldReturnFalse_whenOfferIsNull() {
        // when
        boolean result = evaluator.isSaturdayFull(null, defaultConstraints);

        // then
        assertFalse(result, "Sobota nie powinna być uznana za pełną dla null offer");
    }

    @Test
    @DisplayName("Null constraints => false")
    void shouldReturnFalse_whenConstraintsIsNull() {
        // given
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, ROME_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, ROME_ZONE);
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ROME_ZONE);
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ROME_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        boolean result = evaluator.isSaturdayFull(offer, null);

        assertFalse(result, "Sobota nie powinna być uznana za pełną dla null constraints");
    }
}

