package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.LocalTime;

public record TripConstraints(
        int maxStops,
        int maxTotalDurationMinutesOneWay,
        Integer hardCapPricePln,
        LocalTime latestArrivalOnFridayLocal,
        LocalTime earliestDepartureOnSundayLocal,
        boolean requireNoFlightOnSaturday
) {
}

