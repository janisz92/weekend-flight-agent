package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.ZonedDateTime;

public record FlightSegment(
        String departureAirport,
        String arrivalAirport,
        ZonedDateTime departureTime,
        ZonedDateTime arrivalTime
) {
}

