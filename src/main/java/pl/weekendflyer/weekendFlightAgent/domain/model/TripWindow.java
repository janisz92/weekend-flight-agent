package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.ZonedDateTime;

/**
 * Okno podróży definiujące parametry wyszukiwania lotu weekendowego.
 * Reprezentuje konkretny przedział czasowy wylotu i powrotu.
 *
 * @param originIata kod IATA lotniska wylotu
 * @param destinationIata kod IATA lotniska docelowego
 * @param outboundDeparture planowany czas wylotu z origin
 * @param inboundDeparture planowany czas powrotu z destination
 * @param desiredFullDays liczba pełnych dni pobytu w miejscu docelowym
 */
public record TripWindow(
        String originIata,
        String destinationIata,
        ZonedDateTime outboundDeparture,
        ZonedDateTime inboundDeparture,
        int desiredFullDays
) {
}

