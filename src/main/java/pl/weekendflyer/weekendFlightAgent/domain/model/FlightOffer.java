package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.time.ZonedDateTime;
import java.util.List;

public record FlightOffer(
        String originIata,
        String destinationIata,
        List<FlightSegment> outboundSegments,
        List<FlightSegment> inboundSegments,
        Integer pricePln,
        String provider,
        String deepLink
) {
    public int totalStops() {
        int outboundStops = Math.max(0, outboundSegments.size() - 1);
        int inboundStops = Math.max(0, inboundSegments.size() - 1);
        return outboundStops + inboundStops;
    }

    public ZonedDateTime outboundArrivalTime() {
        if (outboundSegments.isEmpty()) {
            throw new IllegalStateException("No outbound segments available");
        }
        return outboundSegments.get(outboundSegments.size() - 1).arrivalTime();
    }

    public ZonedDateTime inboundArrivalTime() {
        if (inboundSegments.isEmpty()) {
            throw new IllegalStateException("No inbound segments available");
        }
        return inboundSegments.get(inboundSegments.size() - 1).arrivalTime();
    }
}

