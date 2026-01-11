package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.ZonedDateTime;

/**
 * Pojedynczy segment lotu reprezentujący jeden odcinek podróży.
 * Lot może składać się z jednego lub więcej segmentów (w przypadku przesiadek).
 *
 * @param departureAirport kod IATA lotniska odlotu
 * @param arrivalAirport kod IATA lotniska przylotu
 * @param departureTime czas odlotu
 * @param arrivalTime czas przylotu
 */
public record FlightSegment(
        String departureAirport,
        String arrivalAirport,
        ZonedDateTime departureTime,
        ZonedDateTime arrivalTime
) {
}

