package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.ZonedDateTime;

public record TripWindow(
        String originIata,
        String destinationIata,
        ZonedDateTime outboundDeparture,
        ZonedDateTime inboundDeparture,
        int desiredFullDays
) {
}

