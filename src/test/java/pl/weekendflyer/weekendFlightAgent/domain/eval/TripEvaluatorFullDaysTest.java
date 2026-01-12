package pl.weekendflyer.weekendFlightAgent.domain.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.weekendflyer.weekendFlightAgent.domain.eval.FlightOfferTestHelper.*;

class TripEvaluatorFullDaysTest {

    private TripEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new TripEvaluator();
    }

    @Test
    @DisplayName("Przylot pt 21:00, wylot nd 10:00 => fullDays = 1 (tylko sobota)")
    void shouldReturn1FullDay_whenArrivalFridayEveningDepartureSundayMorning() {
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        assertEquals(1, evaluator.fullDaysOnSite(offer));
    }

    @Test
    @DisplayName("Przylot pt 21:00, wylot pn 10:00 => fullDays = 2 (sobota + niedziela)")
    void shouldReturn2FullDays_whenArrivalFridayEveningDepartureMondayMorning() {
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 19, 13, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        assertEquals(2, evaluator.fullDaysOnSite(offer));
    }

    @Test
    @DisplayName("Przylot czw 23:00, wylot pn 06:00 => fullDays = 3 (pt, sob, nd)")
    void shouldReturn3FullDays_whenArrivalThursdayEveningDepartureMondayMorning() {
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 15, 20, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 15, 23, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 19, 6, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 19, 9, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        assertEquals(3, evaluator.fullDaysOnSite(offer));
    }

    @Test
    @DisplayName("Przylot sob 01:00, wylot pn 10:00 => fullDays = 1 (nd)")
    void shouldReturn1FullDay_whenArrivalSaturdayNightDepartureMondayMorning() {
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 22, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 17, 1, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 19, 13, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        assertEquals(1, evaluator.fullDaysOnSite(offer));
    }

    @Test
    @DisplayName("Brak segmentów outbound => fullDays = 0")
    void shouldReturn0_whenNoOutboundSegments() {
        FlightOffer offer = buildOffer(
                List.of(),
                List.of(createSegment(
                        ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0, LISBON_ZONE),
                        ZonedDateTime.of(2026, 1, 19, 13, 0, 0, 0, LISBON_ZONE)
                )),
                500
        );

        assertEquals(0, evaluator.fullDaysOnSite(offer));
    }

    @Test
    @DisplayName("Brak segmentów inbound => fullDays = 0")
    void shouldReturn0_whenNoInboundSegments() {
        FlightOffer offer = buildOffer(
                List.of(createSegment(
                        ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, LISBON_ZONE),
                        ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, LISBON_ZONE)
                )),
                List.of(),
                500
        );

        assertEquals(0, evaluator.fullDaysOnSite(offer));
    }

    @Test
    @DisplayName("Null offer => fullDays = 0")
    void shouldReturn0_whenOfferIsNull() {
        assertEquals(0, evaluator.fullDaysOnSite(null));
    }
}

