package pl.weekendflyer.weekendFlightAgent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalTime;
import java.util.List;

@ConfigurationProperties(prefix = "agent")
public record AgentProperties(
        String timezone,
        List<String> origins,
        List<String> destinations,
        Search search,
        SaturdayRule saturdayRule,
        Constraints constraints,
        Baseline baseline,
        CandidateFilter candidateFilter,
        Alerts alerts
) {

    public record Search(
            int horizonDays,
            List<Integer> fullDaysAllowed
    ) {}

    public record SaturdayRule(
            boolean requireNoFlightOnSaturday,
            LocalTime latestArrivalOnFridayLocal,
            LocalTime earliestDepartureOnSundayLocal
    ) {}

    public record Constraints(
            int maxStops,
            int maxTotalDurationMinutesOneWay,
            Integer hardCapPricePLN,
            LocalTime earliestDepartureFromOriginLocal,
            LocalTime latestArrivalToOriginLocal
    ) {}

    public record Baseline(
            int rollingDays,
            SegmentKey segmentKey
    ) {
        public record SegmentKey(
                boolean includeDepartureMonth,
                boolean includeFullDays
        ) {}
    }

    public record CandidateFilter(
            int minAbsoluteSavingPLN,
            int minPercentBelowMedian,
            int maxCandidatesPerRun
    ) {}

    public record Alerts(
            String channel,
            int maxAlertsPerDay,
            int maxAlertsPerDestinationPerWeek
    ) {}
}
