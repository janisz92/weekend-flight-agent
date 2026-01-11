package pl.weekendflyer.weekendFlightAgent.domain.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightSegment;
import pl.weekendflyer.weekendFlightAgent.domain.model.TripConstraints;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Ewaluator ofert lotów weekendowych.
 * Deterministycznie ocenia FlightOffer pod kątem spełnienia reguł i ograniczeń weekendowych.
 *
 * <p>Klasa dostarcza metody do oceny:
 * <ul>
 *   <li>Liczby pełnych dni spędzonych w destynacji (fullDaysOnSite)</li>
 *   <li>Zgodności z regułami soboty weekendowej (isSaturdayFull)</li>
 *   <li>Spełnienia twardych ograniczeń: przesiadki, czas lotu, cena (meetsHardConstraints)</li>
 * </ul>
 *
 * <p><b>Ważne:</b> Wszystkie operacje używają strefy czasowej destynacji do obliczania dni kalendarzowych
 * i godzin przylotu/wylotu. Strefa jest pobierana z ZonedDateTime segmentów lotu.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Przykład użycia
 * TripEvaluator evaluator = new TripEvaluator();
 *
 * // Przygotowanie oferty lotu
 * FlightSegment outbound = new FlightSegment(
 *     "WAW", "BCN",
 *     ZonedDateTime.of(2026, 1, 16, 18, 0, 0, 0, ZoneId.of("Europe/Barcelona")),
 *     ZonedDateTime.of(2026, 1, 16, 21, 0, 0, 0, ZoneId.of("Europe/Barcelona"))
 * );
 *
 * FlightSegment inbound = new FlightSegment(
 *     "BCN", "WAW",
 *     ZonedDateTime.of(2026, 1, 18, 10, 0, 0, 0, ZoneId.of("Europe/Barcelona")),
 *     ZonedDateTime.of(2026, 1, 18, 13, 0, 0, 0, ZoneId.of("Europe/Barcelona"))
 * );
 *
 * FlightOffer offer = new FlightOffer(
 *     "WAW", "BCN",
 *     List.of(outbound),
 *     List.of(inbound),
 *     1500, // cena PLN
 *     "TestProvider",
 *     "https://example.com/offer"
 * );
 *
 * TripConstraints constraints = new TripConstraints(
 *     1,                          // maxStops
 *     480,                        // maxTotalDurationMinutesOneWay (8h)
 *     2000,                       // hardCapPricePln
 *     LocalTime.of(22, 0),       // latestArrivalOnFridayLocal
 *     LocalTime.of(6, 0),        // earliestDepartureOnSundayLocal
 *     true                        // requireNoFlightOnSaturday
 * );
 *
 * // Ocena oferty
 * int fullDays = evaluator.fullDaysOnSite(offer);               // => 1 (sobota)
 * boolean saturdayOk = evaluator.isSaturdayFull(offer, constraints);  // => true
 * boolean meetsLimits = evaluator.meetsHardConstraints(offer, constraints); // => true
 * }</pre>
 *
 * @see FlightOffer
 * @see TripConstraints
 */
@Component
public class TripEvaluator {

    private static final Logger log = LoggerFactory.getLogger(TripEvaluator.class);

    /**
     * Oblicza liczbę pełnych dni spędzonych w miejscu docelowym.
     *
     * <p><b>Algorytm liczenia pełnych dni:</b>
     * <ol>
     *   <li>Pobieramy czas przylotu do destynacji (ostatni segment outbound) i konwertujemy na LocalDate w strefie destynacji</li>
     *   <li>Pobieramy czas wylotu z destynacji (pierwszy segment inbound) i konwertujemy na LocalDate w strefie destynacji</li>
     *   <li>Pełny dzień to dzień kalendarzowy między dniem po przylocie a dniem wylotu (exclusive)</li>
     *   <li>Liczymy dni między (arrivalLocalDate + 1) a (departureLocalDate)</li>
     * </ol>
     *
     * <p><b>Przykłady:</b>
     * <ul>
     *   <li>Przylot pt 21:00, wylot nd 10:00 → fullDays = 1 (sobota)</li>
     *   <li>Przylot pt 21:00, wylot pn 10:00 → fullDays = 2 (sobota + niedziela)</li>
     *   <li>Przylot czw 23:00, wylot pn 06:00 → fullDays = 3 (piątek + sobota + niedziela)</li>
     *   <li>Przylot sob 01:00, wylot pn 10:00 → fullDays = 1 (niedziela)</li>
     * </ul>
     *
     * <p><b>Strefa czasowa:</b> Wszystkie obliczenia wykonywane są w strefie czasowej destynacji,
     * która jest pobierana z {@code offer.outboundArrivalTime().getZone()}.
     *
     * @param offer oferta lotu
     * @return liczba pełnych dni >= 0, lub 0 jeśli offer jest null lub brakuje segmentów
     */
    public int fullDaysOnSite(FlightOffer offer) {
        if (offer == null || offer.outboundSegments().isEmpty() || offer.inboundSegments().isEmpty()) {
            log.debug("fullDaysOnSite: brak segmentów, zwracam 0");
            return 0;
        }

        // Pobieramy czasy przylotu i wylotu w strefie destynacji
        // Strefa czasowa jest zawarta w ZonedDateTime segmentów
        ZonedDateTime arrivalAtDestination = offer.outboundArrivalTime();
        ZonedDateTime departureFromDestination = offer.inboundSegments().get(0).departureTime();

        // Konwertujemy na LocalDate w strefie destynacji
        LocalDate arrivalLocalDate = arrivalAtDestination.toLocalDate();
        LocalDate departureLocalDate = departureFromDestination.toLocalDate();

        // Pełne dni = dni między (arrivalLocalDate + 1) a (departureLocalDate) [exclusive]
        // Przykład: przylot pt, wylot nd => pełny dzień to sobota (1 dzień)
        LocalDate firstFullDay = arrivalLocalDate.plusDays(1);

        long fullDays = 0;
        if (departureLocalDate.isAfter(firstFullDay)) {
            fullDays = firstFullDay.datesUntil(departureLocalDate).count();
        }

        log.debug("fullDaysOnSite: arrival={}, departure={}, fullDays={}",
                arrivalLocalDate, departureLocalDate, fullDays);

        return (int) fullDays;
    }

    /**
     * Sprawdza czy sobota jest w pełni wolna od lotów zgodnie z wymogami weekendowymi.
     *
     * <p><b>Warunki do spełnienia (wszystkie muszą być prawdziwe):</b>
     * <ol>
     *   <li><b>Dzień przylotu:</b> Przylot do destynacji musi być w piątek (w strefie destynacji)</li>
     *   <li><b>Godzina przylotu:</b> Przylot nie później niż {@code constraints.latestArrivalOnFridayLocal}
     *       (np. 22:00 czasu lokalnego destynacji)</li>
     *   <li><b>Dzień wylotu:</b> Wylot z destynacji musi być w niedzielę (w strefie destynacji)</li>
     *   <li><b>Godzina wylotu:</b> Wylot nie wcześniej niż {@code constraints.earliestDepartureOnSundayLocal}
     *       (np. 06:00 czasu lokalnego destynacji)</li>
     *   <li><b>Brak lotów w sobotę (opcjonalnie):</b> Jeśli {@code constraints.requireNoFlightOnSaturday == true},
     *       to żaden segment (outbound ani inbound) nie może mieć departure ani arrival w sobotę w strefie destynacji</li>
     * </ol>
     *
     * <p><b>Strefy czasowe:</b>
     * <ul>
     *   <li>Dni tygodnia i godziny są sprawdzane w strefie czasowej destynacji</li>
     *   <li>Strefa destynacji jest pobierana z {@code offer.outboundArrivalTime().getZone()}</li>
     *   <li>Wszystkie segmenty są konwertowane do tej strefy przy sprawdzaniu warunku soboty</li>
     *   <li>Dzięki temu, jeśli lot startuje z WAW o 23:00 pt i ląduje w BCN o 01:00 sob (czasu lokalnego BCN),
     *       to zostanie wykryty jako lot w sobotę</li>
     * </ul>
     *
     * <p><b>Przykłady:</b>
     * <ul>
     *   <li>Przylot pt 21:59, wylot nd 06:00, brak lotów w sobotę → true</li>
     *   <li>Przylot pt 22:01 → false (po progu)</li>
     *   <li>Wylot nd 05:59 → false (przed progiem)</li>
     *   <li>Przylot w sobotę 00:10 → false (nie jest piątek + lot w sobotę)</li>
     * </ul>
     *
     * @param offer oferta lotu
     * @param constraints ograniczenia zawierające progi czasowe i flagę requireNoFlightOnSaturday
     * @return true jeśli sobota spełnia wszystkie wymagania weekendowe, false w przeciwnym razie
     */
    public boolean isSaturdayFull(FlightOffer offer, TripConstraints constraints) {
        if (offer == null || constraints == null) {
            log.debug("isSaturdayFull: null offer lub constraints, zwracam false");
            return false;
        }

        if (offer.outboundSegments().isEmpty() || offer.inboundSegments().isEmpty()) {
            log.debug("isSaturdayFull: brak segmentów, zwracam false");
            return false;
        }

        ZonedDateTime arrivalAtDestination = offer.outboundArrivalTime();
        ZonedDateTime departureFromDestination = offer.inboundSegments().get(0).departureTime();

        // Warunek b) przylot do destynacji nie później niż latestArrivalOnFridayLocal w piątek
        LocalDate arrivalLocalDate = arrivalAtDestination.toLocalDate();
        if (arrivalLocalDate.getDayOfWeek() != DayOfWeek.FRIDAY) {
            log.debug("isSaturdayFull: przylot nie jest w piątek ({}), zwracam false", arrivalLocalDate.getDayOfWeek());
            return false;
        }

        if (arrivalAtDestination.toLocalTime().isAfter(constraints.latestArrivalOnFridayLocal())) {
            log.debug("isSaturdayFull: przylot w piątek po {}, zwracam false", constraints.latestArrivalOnFridayLocal());
            return false;
        }

        // Warunek c) wylot z destynacji nie wcześniej niż earliestDepartureOnSundayLocal w niedzielę
        LocalDate departureLocalDate = departureFromDestination.toLocalDate();
        if (departureLocalDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            log.debug("isSaturdayFull: wylot powrotny nie jest w niedzielę ({}), zwracam false", departureLocalDate.getDayOfWeek());
            return false;
        }

        if (departureFromDestination.toLocalTime().isBefore(constraints.earliestDepartureOnSundayLocal())) {
            log.debug("isSaturdayFull: wylot w niedzielę przed {}, zwracam false", constraints.earliestDepartureOnSundayLocal());
            return false;
        }

        // Warunek a) jeśli requireNoFlightOnSaturday == true, żaden segment nie może mieć departure/arrival w sobotę
        if (constraints.requireNoFlightOnSaturday()) {
            boolean hasFlightOnSaturday = hasAnySegmentOnSaturday(offer, arrivalAtDestination.getZone());
            if (hasFlightOnSaturday) {
                log.debug("isSaturdayFull: wykryto lot w sobotę, zwracam false");
                return false;
            }
        }

        log.debug("isSaturdayFull: wszystkie warunki spełnione, zwracam true");
        return true;
    }

    /**
     * Sprawdza czy oferta spełnia wszystkie twarde ograniczenia.
     *
     * <p><b>Sprawdzane ograniczenia:</b>
     * <ol>
     *   <li><b>Liczba przesiadek (maxStops):</b> Sprawdzane osobno dla outbound i inbound.
     *       Przesiadki = liczba segmentów - 1. Np. 2 segmenty = 1 przesiadka.
     *       Musi być: {@code outboundStops <= maxStops} AND {@code inboundStops <= maxStops}</li>
     *
     *   <li><b>Czas trwania lotu (maxTotalDurationMinutesOneWay):</b> Sprawdzane osobno dla outbound i inbound.
     *       Czas liczony od departure pierwszego segmentu do arrival ostatniego segmentu (włącznie z przesiadkami).
     *       Musi być: {@code outboundDuration <= max} AND {@code inboundDuration <= max}</li>
     *
     *   <li><b>Limit cenowy (hardCapPricePln):</b> Jeśli nie-null, to cena oferty musi być <= cap.
     *       Jeśli null, to brak ograniczenia cenowego.</li>
     * </ol>
     *
     * <p><b>Obliczanie czasu trwania one-way:</b>
     * Czas trwania to różnica między:
     * <ul>
     *   <li>Start: {@code segments.get(0).departureTime()} - wylot z pierwszego lotniska</li>
     *   <li>End: {@code segments.get(last).arrivalTime()} - przylot na ostatnie lotnisko</li>
     *   <li>Używamy {@code Duration.between(start, end).toMinutes()}</li>
     *   <li>Uwzględnia to automatycznie wszystkie przesiadki i strefy czasowe</li>
     * </ul>
     *
     * <p><b>Przykłady:</b>
     * <ul>
     *   <li>1 segment 2h, cena 1500, maxStops=1, maxDuration=480, cap=2000 → true</li>
     *   <li>2 segmenty (08:00→11:00, 12:00→15:00 = 7h), maxDuration=480 (8h) → true</li>
     *   <li>3 segmenty (2 przesiadki), maxStops=1 → false</li>
     *   <li>1 segment 9h, maxDuration=480 (8h) → false</li>
     *   <li>Cena 2500, cap=2000 → false</li>
     * </ul>
     *
     * @param offer oferta lotu
     * @param constraints ograniczenia zawierające maxStops, maxTotalDurationMinutesOneWay, hardCapPricePln
     * @return true jeśli wszystkie ograniczenia spełnione, false jeśli którekolwiek naruszono lub brak danych
     */
    public boolean meetsHardConstraints(FlightOffer offer, TripConstraints constraints) {
        if (offer == null || constraints == null) {
            log.debug("meetsHardConstraints: null offer lub constraints, zwracam false");
            return false;
        }

        if (offer.outboundSegments().isEmpty() || offer.inboundSegments().isEmpty()) {
            log.debug("meetsHardConstraints: brak segmentów, zwracam false");
            return false;
        }

        if (offer.pricePln() == null) {
            log.debug("meetsHardConstraints: brak ceny, zwracam false");
            return false;
        }

        // Sprawdzenie maxStops (dla każdego kierunku osobno)
        // Liczba przesiadek = liczba segmentów - 1
        // Przykład: 2 segmenty = 1 przesiadka, 3 segmenty = 2 przesiadki
        int outboundStops = offer.outboundSegments().size() - 1;
        int inboundStops = offer.inboundSegments().size() - 1;

        if (outboundStops > constraints.maxStops()) {
            log.debug("meetsHardConstraints: za dużo przesiadek outbound ({} > {})", outboundStops, constraints.maxStops());
            return false;
        }

        if (inboundStops > constraints.maxStops()) {
            log.debug("meetsHardConstraints: za dużo przesiadek inbound ({} > {})", inboundStops, constraints.maxStops());
            return false;
        }

        // Sprawdzenie maxTotalDurationMinutesOneWay
        // Czas liczony od departure pierwszego segmentu do arrival ostatniego segmentu
        // Uwzględnia czas lotu + czas oczekiwania na przesiadki
        long outboundDuration = durationMinutes(offer.outboundSegments());
        long inboundDuration = durationMinutes(offer.inboundSegments());

        if (outboundDuration > constraints.maxTotalDurationMinutesOneWay()) {
            log.debug("meetsHardConstraints: za długi czas outbound ({} > {} min)",
                    outboundDuration, constraints.maxTotalDurationMinutesOneWay());
            return false;
        }

        if (inboundDuration > constraints.maxTotalDurationMinutesOneWay()) {
            log.debug("meetsHardConstraints: za długi czas inbound ({} > {} min)",
                    inboundDuration, constraints.maxTotalDurationMinutesOneWay());
            return false;
        }

        // Sprawdzenie hardCapPricePln
        if (constraints.hardCapPricePln() != null && offer.pricePln() > constraints.hardCapPricePln()) {
            log.debug("meetsHardConstraints: cena za wysoka ({} > {})",
                    offer.pricePln(), constraints.hardCapPricePln());
            return false;
        }

        log.debug("meetsHardConstraints: wszystkie ograniczenia spełnione, zwracam true");
        return true;
    }

    /**
     * Oblicza całkowity czas trwania podróży w minutach od startu pierwszego segmentu
     * do lądowania ostatniego segmentu.
     *
     * <p>Czas trwania obejmuje:
     * <ul>
     *   <li>Czas wszystkich lotów</li>
     *   <li>Czas oczekiwania na przesiadki</li>
     *   <li>Różnice stref czasowych (automatycznie obsługiwane przez ZonedDateTime)</li>
     * </ul>
     *
     * <p><b>Przykład:</b> Dla segmentów:
     * <ul>
     *   <li>Segment 1: WAW 08:00 → FRA 10:00</li>
     *   <li>Segment 2: FRA 12:00 → BCN 15:00</li>
     *   <li>Czas trwania: 7h (08:00 → 15:00), włącznie z 2h oczekiwania we Frankfurcie</li>
     * </ul>
     *
     * @param segments lista segmentów lotu (w kolejności chronologicznej)
     * @return czas trwania w minutach, lub 0 jeśli lista jest pusta
     */
    private long durationMinutes(List<FlightSegment> segments) {
        if (segments.isEmpty()) {
            return 0;
        }

        // Pobieramy czas startu pierwszego segmentu i lądowania ostatniego
        ZonedDateTime start = segments.get(0).departureTime();
        ZonedDateTime end = segments.get(segments.size() - 1).arrivalTime();

        // Duration.between automatycznie obsługuje różnice stref czasowych
        // i oblicza rzeczywisty czas trwania podróży
        return Duration.between(start, end).toMinutes();
    }

    /**
     * Sprawdza czy którykolwiek segment ma departure lub arrival w sobotę
     * w lokalnej strefie czasowej destynacji.
     *
     * <p><b>Konwersja stref czasowych:</b>
     * Każdy segment jest konwertowany do strefy czasowej destynacji za pomocą
     * {@code withZoneSameInstant()}, co zapewnia poprawne określenie dnia tygodnia
     * w kontekście miejsca docelowego.
     *
     * <p><b>Przykład:</b> Lot z WAW do BCN:
     * <ul>
     *   <li>Departure: WAW piątek 23:00 (Europe/Warsaw)</li>
     *   <li>Arrival: BCN sobota 01:00 (Europe/Barcelona)</li>
     *   <li>Po konwersji arrival do strefy BCN → sobota → zwraca true</li>
     * </ul>
     *
     * @param offer oferta lotu zawierająca segmenty outbound i inbound
     * @param destinationZone strefa czasowa destynacji do której konwertowane są wszystkie czasy
     * @return true jeśli wykryto departure lub arrival w sobotę (w strefie destynacji), false w przeciwnym razie
     */
    private boolean hasAnySegmentOnSaturday(FlightOffer offer, java.time.ZoneId destinationZone) {
        // Sprawdzenie wszystkich segmentów outbound
        for (FlightSegment segment : offer.outboundSegments()) {
            // Konwertujemy czasy segmentu do strefy destynacji
            // withZoneSameInstant zachowuje moment w czasie, ale zmienia strefę
            LocalDate depDate = segment.departureTime().withZoneSameInstant(destinationZone).toLocalDate();
            LocalDate arrDate = segment.arrivalTime().withZoneSameInstant(destinationZone).toLocalDate();

            if (depDate.getDayOfWeek() == DayOfWeek.SATURDAY || arrDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                return true;
            }
        }

        // Sprawdzenie wszystkich segmentów inbound
        for (FlightSegment segment : offer.inboundSegments()) {
            LocalDate depDate = segment.departureTime().withZoneSameInstant(destinationZone).toLocalDate();
            LocalDate arrDate = segment.arrivalTime().withZoneSameInstant(destinationZone).toLocalDate();

            if (depDate.getDayOfWeek() == DayOfWeek.SATURDAY || arrDate.getDayOfWeek() == DayOfWeek.SATURDAY) {
                return true;
            }
        }

        return false;
    }
}

