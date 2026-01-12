package pl.weekendflyer.weekendFlightAgent.domain.eval;

import pl.weekendflyer.weekendFlightAgent.domain.model.FlightOffer;
import pl.weekendflyer.weekendFlightAgent.domain.model.FlightSegment;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

final class FlightOfferTestHelper {

    static final ZoneId LISBON_ZONE = ZoneId.of("Europe/Lisbon");
    static final ZoneId ROME_ZONE = ZoneId.of("Europe/Rome");
    static final ZoneId WARSAW_ZONE = ZoneId.of("Europe/Warsaw");
    static final String ORIGIN = "WAW";
    static final String DESTINATION_LIS = "LIS";
    static final String DESTINATION_FCO = "FCO";
    static final String DESTINATION_BCN = "BCN";

    private FlightOfferTestHelper() {}

    static FlightOffer buildOffer(ZonedDateTime outboundDep, ZonedDateTime outboundArr,
                                   ZonedDateTime inboundDep, ZonedDateTime inboundArr) {
        return buildOffer(outboundDep, outboundArr, inboundDep, inboundArr, 500);
    }

    static FlightOffer buildOffer(ZonedDateTime outboundDep, ZonedDateTime outboundArr,
                                   ZonedDateTime inboundDep, ZonedDateTime inboundArr, Integer price) {
        return buildOffer(
                List.of(createSegment(outboundDep, outboundArr)),
                List.of(createSegment(inboundDep, inboundArr)),
                price
        );
    }

    static FlightOffer buildOffer(List<FlightSegment> outbound, List<FlightSegment> inbound, Integer price) {
        return new FlightOffer(
                ORIGIN,
                DESTINATION_BCN,
                outbound,
                inbound,
                price,
                "TestProvider",
                "https://test.com"
        );
    }

    static FlightSegment createSegment(ZonedDateTime departure, ZonedDateTime arrival) {
        return new FlightSegment(ORIGIN, DESTINATION_BCN, departure, arrival);
    }
}

