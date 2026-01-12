package pl.weekendflyer.weekendFlightAgent.domain.eval;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightSegment;
import pl.weekendflyer.weekendFlightAgent.domain.model.TripConstraints;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
public class TripEvaluator {

    private static final String OUTBOUND = "outbound";
    private static final String INBOUND = "inbound";

    public int fullDaysOnSite(FlightOffer offer) {
        if (!hasRequiredSegments(offer)) {
            log.debug("fullDaysOnSite: brak segmentów, zwracam 0");
            return 0;
        }

        ZoneId destZone = destinationZone(offer);
        ZonedDateTime arrivalAtDestination = toDestZone(offer.outboundArrivalTime(), destZone);
        ZonedDateTime departureFromDestination = toDestZone(offer.inboundSegments().get(0).departureTime(), destZone);

        LocalDate arrivalLocalDate = arrivalAtDestination.toLocalDate();
        LocalDate departureLocalDate = departureFromDestination.toLocalDate();
        LocalDate firstFullDay = arrivalLocalDate.plusDays(1);

        long fullDays = departureLocalDate.isAfter(firstFullDay)
                ? firstFullDay.datesUntil(departureLocalDate).count()
                : 0;

        log.debug("fullDaysOnSite: arrival={}, departure={}, fullDays={}", arrivalLocalDate, departureLocalDate, fullDays);
        return (int) fullDays;
    }

    public boolean isSaturdayFull(FlightOffer offer, TripConstraints constraints) {
        if (offer == null || constraints == null || !hasRequiredSegments(offer)) {
            log.debug("isSaturdayFull: nieprawidłowe dane wejściowe, zwracam false");
            return false;
        }

        ZoneId destZone = destinationZone(offer);
        ZonedDateTime arrivalAtDestination = offer.outboundArrivalTime();
        ZonedDateTime departureFromDestination = offer.inboundSegments().get(0).departureTime();

        if (constraints.requireNoFlightOnSaturday() && hasAnyFlightOnSaturday(offer, destZone)) {
            log.debug("isSaturdayFull: wykryto lot w sobotę, zwracam false");
            return false;
        }

        if (!isFridayBeforeOrAt(arrivalAtDestination, constraints.latestArrivalOnFridayLocal(), destZone)) {
            log.debug("isSaturdayFull: przylot nie spełnia warunku piątek <= {}", constraints.latestArrivalOnFridayLocal());
            return false;
        }

        if (!isSundayAtOrAfter(departureFromDestination, constraints.earliestDepartureOnSundayLocal(), destZone)) {
            log.debug("isSaturdayFull: wylot nie spełnia warunku niedziela >= {}", constraints.earliestDepartureOnSundayLocal());
            return false;
        }

        log.debug("isSaturdayFull: wszystkie warunki spełnione");
        return true;
    }

    public boolean meetsHardConstraints(FlightOffer offer, TripConstraints constraints) {
        if (offer == null || constraints == null || !hasRequiredSegments(offer)) {
            log.debug("meetsHardConstraints: nieprawidłowe dane wejściowe, zwracam false");
            return false;
        }

        if (offer.pricePln() == null) {
            log.debug("meetsHardConstraints: brak ceny, zwracam false");
            return false;
        }

        if (!withinStops(offer.outboundSegments(), constraints.maxStops(), OUTBOUND) ||
            !withinStops(offer.inboundSegments(), constraints.maxStops(), INBOUND)) {
            return false;
        }

        if (!withinDuration(offer.outboundSegments(), constraints.maxTotalDurationMinutesOneWay(), OUTBOUND) ||
            !withinDuration(offer.inboundSegments(), constraints.maxTotalDurationMinutesOneWay(), INBOUND)) {
            return false;
        }

        if (!withinPriceCap(offer.pricePln(), constraints.hardCapPricePln())) {
            return false;
        }

        log.debug("meetsHardConstraints: wszystkie ograniczenia spełnione");
        return true;
    }

    private boolean hasRequiredSegments(FlightOffer offer) {
        return offer != null && !offer.outboundSegments().isEmpty() && !offer.inboundSegments().isEmpty();
    }

    private long durationMinutes(List<FlightSegment> segments) {
        if (segments.isEmpty()) {
            return 0;
        }
        ZonedDateTime start = segments.get(0).departureTime();
        ZonedDateTime end = segments.get(segments.size() - 1).arrivalTime();
        return Duration.between(start, end).toMinutes();
    }

    private ZoneId destinationZone(FlightOffer offer) {
        return offer.outboundArrivalTime().getZone();
    }

    private ZonedDateTime toDestZone(ZonedDateTime time, ZoneId destZone) {
        return time.withZoneSameInstant(destZone);
    }

    private boolean isSaturday(ZonedDateTime time, ZoneId destZone) {
        return toDestZone(time, destZone).getDayOfWeek() == DayOfWeek.SATURDAY;
    }

    private boolean isFridayBeforeOrAt(ZonedDateTime arrival, LocalTime latestFriday, ZoneId destZone) {
        ZonedDateTime localArrival = toDestZone(arrival, destZone);
        return localArrival.getDayOfWeek() == DayOfWeek.FRIDAY &&
               !localArrival.toLocalTime().isAfter(latestFriday);
    }

    private boolean isSundayAtOrAfter(ZonedDateTime departure, LocalTime earliestSunday, ZoneId destZone) {
        ZonedDateTime localDeparture = toDestZone(departure, destZone);
        return localDeparture.getDayOfWeek() == DayOfWeek.SUNDAY &&
               !localDeparture.toLocalTime().isBefore(earliestSunday);
    }

    private int stops(List<FlightSegment> segments) {
        return Math.max(0, segments.size() - 1);
    }

    private boolean withinStops(List<FlightSegment> segments, int maxStops, String direction) {
        int actualStops = stops(segments);
        if (actualStops > maxStops) {
            log.debug("meetsHardConstraints: za dużo przesiadek {} ({} > {})", direction, actualStops, maxStops);
            return false;
        }
        return true;
    }

    private boolean withinDuration(List<FlightSegment> segments, int maxDurationMinutes, String direction) {
        long actualDuration = durationMinutes(segments);
        if (actualDuration > maxDurationMinutes) {
            log.debug("meetsHardConstraints: za długi czas {} ({} > {} min)", direction, actualDuration, maxDurationMinutes);
            return false;
        }
        return true;
    }

    private boolean withinPriceCap(Integer pricePln, Integer hardCapPricePln) {
        if (hardCapPricePln != null && pricePln > hardCapPricePln) {
            log.debug("meetsHardConstraints: cena za wysoka ({} > {})", pricePln, hardCapPricePln);
            return false;
        }
        return true;
    }

    private boolean hasAnyFlightOnSaturday(FlightOffer offer, ZoneId destinationZone) {
        for (FlightSegment segment : offer.outboundSegments()) {
            if (isSaturday(segment.departureTime(), destinationZone) ||
                isSaturday(segment.arrivalTime(), destinationZone)) {
                return true;
            }
        }
        for (FlightSegment segment : offer.inboundSegments()) {
            if (isSaturday(segment.departureTime(), destinationZone) ||
                isSaturday(segment.arrivalTime(), destinationZone)) {
                return true;
            }
        }

        return false;
    }
}

