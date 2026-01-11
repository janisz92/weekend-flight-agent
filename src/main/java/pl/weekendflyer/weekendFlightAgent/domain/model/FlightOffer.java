package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Oferta lotu w obie strony (round trip) składająca się z segmentów wylotu i powrotu.
 * Zawiera informacje o cenie, dostawcy oraz linku do oferty.
 *
 * @param originIata kod IATA lotniska początkowego
 * @param destinationIata kod IATA lotniska docelowego
 * @param outboundSegments lista segmentów lotu w kierunku destination
 * @param inboundSegments lista segmentów lotu powrotnego do origin
 * @param pricePln cena łączna w PLN
 * @param provider nazwa dostawcy oferty (np. nazwa API)
 * @param deepLink URL prowadzący do szczegółów oferty u dostawcy
 */
public record FlightOffer(
        String originIata,
        String destinationIata,
        List<FlightSegment> outboundSegments,
        List<FlightSegment> inboundSegments,
        Integer pricePln,
        String provider,
        String deepLink
) {
    /**
     * Zwraca łączną liczbę przesiadek w obu kierunkach.
     * Przesiadki to liczba segmentów minus 1 w każdym kierunku.
     *
     * @return suma przesiadek outbound + inbound
     */
    public int totalStops() {
        int outboundStops = Math.max(0, outboundSegments.size() - 1);
        int inboundStops = Math.max(0, inboundSegments.size() - 1);
        return outboundStops + inboundStops;
    }

    /**
     * Zwraca czas przylotu ostatniego segmentu lotu w kierunku destination.
     *
     * @return czas przylotu do miejsca docelowego
     * @throws IllegalStateException jeśli brak segmentów outbound
     */
    public ZonedDateTime outboundArrivalTime() {
        if (outboundSegments.isEmpty()) {
            throw new IllegalStateException("No outbound segments available");
        }
        return outboundSegments.get(outboundSegments.size() - 1).arrivalTime();
    }

    /**
     * Zwraca czas przylotu ostatniego segmentu lotu powrotnego.
     *
     * @return czas przylotu do miejsca początkowego
     * @throws IllegalStateException jeśli brak segmentów inbound
     */
    public ZonedDateTime inboundArrivalTime() {
        if (inboundSegments.isEmpty()) {
            throw new IllegalStateException("No inbound segments available");
        }
        return inboundSegments.get(inboundSegments.size() - 1).arrivalTime();
    }
}

