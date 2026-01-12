package pl.weekendflyer.weekendFlightAgent.domain.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightSegment;
import pl.weekendflyer.weekendFlightAgent.domain.model.TripConstraints;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pl.weekendflyer.weekendFlightAgent.domain.eval.FlightOfferTestHelper.*;

class TripEvaluatorHardConstraintsTest {

    private TripEvaluator evaluator;
    private TripConstraints constraints;

    @BeforeEach
    void setUp() {
        evaluator = new TripEvaluator();
        constraints = new TripConstraints(
                1, 480, 2000,
                LocalTime.of(22, 0), LocalTime.of(6, 0), true
        );
    }

    @Test
    @DisplayName("Outbound 1 segment 2h, inbound 1 segment 2h, price=1500 => true")
    void shouldReturnTrue_whenAllConstraintsMet() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, 1500);

        assertTrue(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Outbound 2 segmenty (1 stop) sumarycznie 7h, inbound 1 segment 2h => true")
    void shouldReturnTrue_whenOneStopAndDurationUnderLimit() {
        ZonedDateTime outboundSeg1Dep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg1Arr = ZonedDateTime.of(2026, 1, 16, 11, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg2Dep = ZonedDateTime.of(2026, 1, 16, 12, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg2Arr = ZonedDateTime.of(2026, 1, 16, 15, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundSeg1Dep, outboundSeg1Arr), createSegment(outboundSeg2Dep, outboundSeg2Arr)),
                List.of(createSegment(inboundDep, inboundArr)),
                1800
        );

        assertTrue(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Outbound 3 segmenty (2 stops) => false")
    void shouldReturnFalse_whenTooManyStops() {
        ZonedDateTime outboundSeg1Dep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg1Arr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg2Dep = ZonedDateTime.of(2026, 1, 16, 11, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg2Arr = ZonedDateTime.of(2026, 1, 16, 13, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg3Dep = ZonedDateTime.of(2026, 1, 16, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundSeg3Arr = ZonedDateTime.of(2026, 1, 16, 16, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(
                List.of(
                        createSegment(outboundSeg1Dep, outboundSeg1Arr),
                        createSegment(outboundSeg2Dep, outboundSeg2Arr),
                        createSegment(outboundSeg3Dep, outboundSeg3Arr)
                ),
                List.of(createSegment(inboundDep, inboundArr)),
                1500
        );

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Outbound duration 9h => false")
    void shouldReturnFalse_whenDurationExceedsLimit() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 17, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, 1500);

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Price=2500 => false")
    void shouldReturnFalse_whenPriceExceedsCap() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, 2500);

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Null price => false")
    void shouldReturnFalse_whenPriceIsNull() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, null);

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Brak segmentów outbound => false")
    void shouldReturnFalse_whenNoOutboundSegments() {
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(List.of(), List.of(createSegment(inboundDep, inboundArr)), 1500);

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Brak segmentów inbound => false")
    void shouldReturnFalse_whenNoInboundSegments() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(List.of(createSegment(outboundDep, outboundArr)), List.of(), 1500);

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Null offer => false")
    void shouldReturnFalse_whenOfferIsNull() {
        assertFalse(evaluator.meetsHardConstraints(null, constraints));
    }

    @Test
    @DisplayName("Null constraints => false")
    void shouldReturnFalse_whenConstraintsIsNull() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, 1500);

        assertFalse(evaluator.meetsHardConstraints(offer, null));
    }

    @Test
    @DisplayName("HardCapPricePln=null (brak limitu), price=2500 => true")
    void shouldReturnTrue_whenNoPriceCap() {
        TripConstraints noPriceCapConstraints = new TripConstraints(
                1, 480, null, LocalTime.of(22, 0), LocalTime.of(6, 0), true
        );

        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, 2500);

        assertTrue(evaluator.meetsHardConstraints(offer, noPriceCapConstraints));
    }

    @Test
    @DisplayName("Inbound z 2 przesiadkami (3 segmenty) => false")
    void shouldReturnFalse_whenInboundHasTooManyStops() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);

        ZonedDateTime inboundSeg1Dep = ZonedDateTime.of(2026, 1, 18, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundSeg1Arr = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundSeg2Dep = ZonedDateTime.of(2026, 1, 18, 11, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundSeg2Arr = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundSeg3Dep = ZonedDateTime.of(2026, 1, 18, 14, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundSeg3Arr = ZonedDateTime.of(2026, 1, 18, 16, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(
                        createSegment(inboundSeg1Dep, inboundSeg1Arr),
                        createSegment(inboundSeg2Dep, inboundSeg2Arr),
                        createSegment(inboundSeg3Dep, inboundSeg3Arr)
                ),
                1500
        );

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }

    @Test
    @DisplayName("Inbound duration 9h => false")
    void shouldReturnFalse_whenInboundDurationExceedsLimit() {
        ZonedDateTime outboundDep = ZonedDateTime.of(2026, 1, 16, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime outboundArr = ZonedDateTime.of(2026, 1, 16, 10, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundDep = ZonedDateTime.of(2026, 1, 18, 8, 0, 0, 0, WARSAW_ZONE);
        ZonedDateTime inboundArr = ZonedDateTime.of(2026, 1, 18, 17, 0, 0, 0, WARSAW_ZONE);

        FlightOffer offer = buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, 1500);

        assertFalse(evaluator.meetsHardConstraints(offer, constraints));
    }
}

