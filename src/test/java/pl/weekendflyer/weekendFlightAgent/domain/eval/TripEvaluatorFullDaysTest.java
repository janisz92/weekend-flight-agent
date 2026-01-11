package pl.weekendflyer.weekendFlightAgent.domain.eval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightSegment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testy jednostkowe dla TripEvaluator.fullDaysOnSite().
 * Weryfikuje poprawność obliczania liczby pełnych dni w destynacji.
 */
class TripEvaluatorFullDaysTest {

    private static final ZoneId LISBON_ZONE = ZoneId.of("Europe/Lisbon");
    private static final String ORIGIN = "WAW";
    private static final String DESTINATION = "LIS";

    private TripEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new TripEvaluator();
    }

    @Test
    @DisplayName("Przylot pt 21:00, wylot nd 10:00 => fullDays = 1 (tylko sobota)")
    void shouldReturn1FullDay_whenArrivalFridayEveningDepartureSundayMorning() {
        // given: przylot piątek 21:00, wylot niedziela 10:00
        // Pełne dni: sobota (1 dzień)
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, LISBON_ZONE); // piątek
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, LISBON_ZONE); // niedziela
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        int fullDays = evaluator.fullDaysOnSite(offer);

        // then
        assertEquals(1, fullDays, "Powinien być 1 pełny dzień (sobota)");
    }

    @Test
    @DisplayName("Przylot pt 21:00, wylot pn 10:00 => fullDays = 2 (sobota + niedziela)")
    void shouldReturn2FullDays_whenArrivalFridayEveningDepartureMondayMorning() {
        // given: przylot piątek 21:00, wylot poniedziałek 10:00
        // Pełne dni: sobota, niedziela (2 dni)
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, LISBON_ZONE); // piątek
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0, LISBON_ZONE); // poniedziałek
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 19, 13, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        int fullDays = evaluator.fullDaysOnSite(offer);

        // then
        assertEquals(2, fullDays, "Powinny być 2 pełne dni (sobota + niedziela)");
    }

    @Test
    @DisplayName("Przylot czw 23:00, wylot pn 06:00 => fullDays = 3 (pt, sob, nd)")
    void shouldReturn3FullDays_whenArrivalThursdayEveningDepartureMondayMorning() {
        // given: przylot czwartek 23:00, wylot poniedziałek 06:00
        // Pełne dni: piątek, sobota, niedziela (3 dni)
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 15, 20, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 15, 23, 0, 0, 0, LISBON_ZONE); // czwartek
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 19, 6, 0, 0, 0, LISBON_ZONE); // poniedziałek
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 19, 9, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        int fullDays = evaluator.fullDaysOnSite(offer);

        // then
        assertEquals(3, fullDays, "Powinny być 3 pełne dni (piątek + sobota + niedziela)");
    }

    @Test
    @DisplayName("Przylot sob 01:00, wylot pn 10:00 => fullDays = 1 (nd)")
    void shouldReturn1FullDay_whenArrivalSaturdayNightDepartureMondayMorning() {
        // given: przylot sobota 01:00, wylot poniedziałek 10:00
        // Pełne dni: niedziela (1 dzień)
        ZonedDateTime outboundDeparture = ZonedDateTime.of(2026, 1, 16, 22, 0, 0, 0, LISBON_ZONE);
        ZonedDateTime outboundArrival = ZonedDateTime.of(2026, 1, 17, 1, 0, 0, 0, LISBON_ZONE); // sobota 01:00
        ZonedDateTime inboundDeparture = ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0, LISBON_ZONE); // poniedziałek
        ZonedDateTime inboundArrival = ZonedDateTime.of(2026, 1, 19, 13, 0, 0, 0, LISBON_ZONE);

        FlightOffer offer = buildOffer(outboundDeparture, outboundArrival, inboundDeparture, inboundArrival);

        // when
        int fullDays = evaluator.fullDaysOnSite(offer);

        // then
        assertEquals(1, fullDays, "Powinien być 1 pełny dzień (niedziela)");
    }

    @Test
    @DisplayName("Brak segmentów outbound => fullDays = 0")
    void shouldReturn0_whenNoOutboundSegments() {
        // given
        FlightOffer offer = new FlightOffer(
                ORIGIN,
                DESTINATION,
                List.of(), // brak segmentów outbound
                List.of(createSegment(
                        ZonedDateTime.of(2026, 1, 19, 10, 0, 0, 0, LISBON_ZONE),
                        ZonedDateTime.of(2026, 1, 19, 13, 0, 0, 0, LISBON_ZONE)
                )),
                500,
                "TestProvider",
                "https://test.com"
        );

        // when
        int fullDays = evaluator.fullDaysOnSite(offer);

        // then
        assertEquals(0, fullDays, "Powinno być 0 pełnych dni przy braku segmentów outbound");
    }

    @Test
    @DisplayName("Brak segmentów inbound => fullDays = 0")
    void shouldReturn0_whenNoInboundSegments() {
        // given
        FlightOffer offer = new FlightOffer(
                ORIGIN,
                DESTINATION,
                List.of(createSegment(
                        ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, LISBON_ZONE),
                        ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, LISBON_ZONE)
                )),
                List.of(), // brak segmentów inbound
                500,
                "TestProvider",
                "https://test.com"
        );

        // when
        int fullDays = evaluator.fullDaysOnSite(offer);

        // then
        assertEquals(0, fullDays, "Powinno być 0 pełnych dni przy braku segmentów inbound");
    }

    @Test
    @DisplayName("Null offer => fullDays = 0")
    void shouldReturn0_whenOfferIsNull() {
        // when
        int fullDays = evaluator.fullDaysOnSite(null);

        // then
        assertEquals(0, fullDays, "Powinno być 0 pełnych dni dla null offer");
    }

    /**
     * Helper do budowania FlightOffer z jednym segmentem outbound i jednym inbound.
     */
    private FlightOffer buildOffer(ZonedDateTime outboundDep, ZonedDateTime outboundArr,
                                     ZonedDateTime inboundDep, ZonedDateTime inboundArr) {
        FlightSegment outboundSegment = createSegment(outboundDep, outboundArr);
        FlightSegment inboundSegment = createSegment(inboundDep, inboundArr);

        return new FlightOffer(
                ORIGIN,
                DESTINATION,
                List.of(outboundSegment),
                List.of(inboundSegment),
                500,
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

