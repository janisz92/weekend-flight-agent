package pl.weekendflyer.weekendFlightAgent.domain.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightSegment;
import pl.weekendflyer.weekendFlightAgent.domain.model.TripConstraints;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testy jednostkowe dla TripEvaluator.meetsHardConstraints().
 * Weryfikuje poprawność sprawdzania twardych ograniczeń dla ofert lotów.
 */
class TripEvaluatorHardConstraintsTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Warsaw");
    private static final String ORIGIN = "WAW";
    private static final String DESTINATION = "BCN";

    private TripEvaluator evaluator;
    private TripConstraints constraints;

    @BeforeEach
    void setUp() {
        evaluator = new TripEvaluator();
        constraints = new TripConstraints(
                1,                              // maxStops
                480,                            // maxTotalDurationMinutesOneWay (8h)
                2000,                           // hardCapPricePln
                LocalTime.of(22, 0),           // latestArrivalOnFridayLocal (nieistotne)
                LocalTime.of(6, 0),            // earliestDepartureOnSundayLocal (nieistotne)
                true                            // requireNoFlightOnSaturday (nieistotne)
        );
    }

    @Test
    @DisplayName("Outbound 1 segment 2h, inbound 1 segment 2h, price=1500 => true")
    void shouldReturnTrue_whenAllConstraintsMet() {
        // given: oferta z 1 segmentem w każdym kierunku, czas 2h każdy, cena 1500
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE); // 2h

        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE); // 2h

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertTrue(result, "Oferta powinna spełniać wszystkie ograniczenia");
    }

    @Test
    @DisplayName("Outbound 2 segmenty (1 stop) sumarycznie 7h, inbound 1 segment 2h => true")
    void shouldReturnTrue_whenOneStopAndDurationUnderLimit() {
        // given: outbound z 1 przesiadką (2 segmenty), łączny czas 7h
        ZonedDateTime outboundSeg1Dep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundSeg1Arr = ZonedDateTime.of(2026, 1, 16, 11, 0, 0, 0, ZONE);

        ZonedDateTime outboundSeg2Dep = ZonedDateTime.of(2026, 1, 16, 12, 0, 0, 0, ZONE);
        ZonedDateTime outboundSeg2Arr = ZonedDateTime.of(2026, 1, 16, 15, 0, 0, 0, ZONE); // total: 7h (8:00-15:00)

        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE); // 2h

        FlightOffer offer = buildOffer(
                List.of(
                        createSegment(outboundSeg1Dep, outboundSeg1Arr),
                        createSegment(outboundSeg2Dep, outboundSeg2Arr)
                ),
                List.of(createSegment(inboundDep, inboundArr)),
                1800
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertTrue(result, "Oferta z 1 przesiadką i 7h podróży powinna spełniać ograniczenia");
    }

    @Test
    @DisplayName("Outbound 3 segmenty (2 stops) => false")
    void shouldReturnFalse_whenTooManyStops() {
        // given: outbound z 2 przesiadkami (3 segmenty) - przekroczenie maxStops=1
        ZonedDateTime outboundSeg1Dep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundSeg1Arr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);

        ZonedDateTime outboundSeg2Dep = ZonedDateTime.of(2026, 1, 16, 11, 0, 0, 0, ZONE);
        ZonedDateTime outboundSeg2Arr = ZonedDateTime.of(2026, 1, 16, 13, 0, 0, 0, ZONE);

        ZonedDateTime outboundSeg3Dep = ZonedDateTime.of(2026, 1, 16, 14, 0, 0, 0, ZONE);
        ZonedDateTime outboundSeg3Arr = ZonedDateTime.of(2026, 1, 16, 16, 0, 0, 0, ZONE); // 3 segmenty = 2 przesiadki

        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(
                        createSegment(outboundSeg1Dep, outboundSeg1Arr),
                        createSegment(outboundSeg2Dep, outboundSeg2Arr),
                        createSegment(outboundSeg3Dep, outboundSeg3Arr)
                ),
                List.of(createSegment(inboundDep, inboundArr)),
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta z 2 przesiadkami nie powinna spełniać ograniczenia maxStops=1");
    }

    @Test
    @DisplayName("Outbound duration 9h (np. 08:00 -> 17:00) => false")
    void shouldReturnFalse_whenDurationExceedsLimit() {
        // given: outbound o czasie trwania 9h - przekroczenie maxTotalDurationMinutesOneWay=480 (8h)
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 17, 0, 0, 0, ZONE); // 9h = 540 min

        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta z czasem lotu 9h nie powinna spełniać ograniczenia 8h");
    }

    @Test
    @DisplayName("Price=2500 => false")
    void shouldReturnFalse_whenPriceExceedsCap() {
        // given: cena 2500 - przekroczenie hardCapPricePln=2000
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);

        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                2500 // ZA DROGO
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta z ceną 2500 nie powinna spełniać ograniczenia cenowego 2000");
    }

    @Test
    @DisplayName("Null price => false")
    void shouldReturnFalse_whenPriceIsNull() {
        // given: oferta bez ceny
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);

        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                null // BRAK CENY
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta bez ceny nie powinna spełniać ograniczeń");
    }

    @Test
    @DisplayName("Brak segmentów outbound => false")
    void shouldReturnFalse_whenNoOutboundSegments() {
        // given: oferta bez segmentów outbound
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(), // BRAK SEGMENTÓW OUTBOUND
                List.of(createSegment(inboundDep, inboundArr)),
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta bez segmentów outbound nie powinna spełniać ograniczeń");
    }

    @Test
    @DisplayName("Brak segmentów inbound => false")
    void shouldReturnFalse_whenNoInboundSegments() {
        // given: oferta bez segmentów inbound
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(), // BRAK SEGMENTÓW INBOUND
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta bez segmentów inbound nie powinna spełniać ograniczeń");
    }

    @Test
    @DisplayName("Null offer => false")
    void shouldReturnFalse_whenOfferIsNull() {
        // when
        boolean result = evaluator.meetsHardConstraints(null, constraints);

        // then
        assertFalse(result, "Null offer nie powinien spełniać ograniczeń");
    }

    @Test
    @DisplayName("Null constraints => false")
    void shouldReturnFalse_whenConstraintsIsNull() {
        // given
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, null);

        // then
        assertFalse(result, "Null constraints nie powinien zostać zaakceptowany");
    }

    @Test
    @DisplayName("HardCapPricePln=null (brak limitu), price=2500 => true")
    void shouldReturnTrue_whenNoPriceCap() {
        // given: constraints bez limitu cenowego
        TripConstraints noPriceCapConstraints = new TripConstraints(
                1,
                480,
                null, // BRAK LIMITU CENOWEGO
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                true
        );

        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                2500 // wysoka cena, ale brak limitu
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, noPriceCapConstraints);

        // then
        assertTrue(result, "Oferta powinna spełniać ograniczenia gdy brak limitu cenowego");
    }

    @Test
    @DisplayName("Inbound z 2 przesiadkami (3 segmenty) => false")
    void shouldReturnFalse_whenInboundHasTooManyStops() {
        // given: inbound z 2 przesiadkami (3 segmenty)
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);

        ZonedDateTime inboundSeg1Dep = ZonedDateTime.of(2026, 1, 18, 8, 0, 0, 0, ZONE);
        ZonedDateTime inboundSeg1Arr = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ZONE);

        ZonedDateTime inboundSeg2Dep = ZonedDateTime.of(2026, 1, 18, 11, 0, 0, 0, ZONE);
        ZonedDateTime inboundSeg2Arr = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ZONE);

        ZonedDateTime inboundSeg3Dep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, ZONE);
        ZonedDateTime inboundSeg3Arr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(
                        createSegment(inboundSeg1Dep, inboundSeg1Arr),
                        createSegment(inboundSeg2Dep, inboundSeg2Arr),
                        createSegment(inboundSeg3Dep, inboundSeg3Arr)
                ),
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta z 2 przesiadkami na inbound nie powinna spełniać ograniczenia maxStops=1");
    }

    @Test
    @DisplayName("Inbound duration 9h => false")
    void shouldReturnFalse_whenInboundDurationExceedsLimit() {
        // given: inbound o czasie trwania 9h
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, ZONE);

        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 8, 0, 0, 0, ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 17, 0, 0, 0, ZONE); // 9h

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                1500
        );

        // when
        boolean result = evaluator.meetsHardConstraints(offer, constraints);

        // then
        assertFalse(result, "Oferta z inbound 9h nie powinna spełniać ograniczenia 8h");
    }

    /**
     * Helper do budowania FlightOffer.
     */
    private FlightOffer buildOffer(List<FlightSegment> outbound, List<FlightSegment> inbound, Integer price) {
        return new FlightOffer(
                ORIGIN,
                DESTINATION,
                outbound,
                inbound,
                price,
                "TestProvider",
                "https://test.com"
        );
    }

    /**
     * Helper do tworzenia FlightSegment.
     */
    private FlightSegment createSegment(ZonedDateTime departure, ZonedDateTime arrival) {
        return new FlightSegment(
                ORIGIN,
                DESTINATION,
                departure,
                arrival
        );
    }
}

