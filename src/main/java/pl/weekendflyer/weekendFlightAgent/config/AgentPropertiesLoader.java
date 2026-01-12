package pl.weekendflyer.weekendFlightAgent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Configuration
public class AgentPropertiesLoader {

    @Bean
    public AgentProperties agentProperties() throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = new ClassPathResource("config.yaml").getInputStream()) {
            Map<String, Object> root = yaml.load(inputStream);
            @SuppressWarnings("unchecked")
            Map<String, Object> agent = (Map<String, Object>) root.get("agent");
            return mapToAgentProperties(agent);
        }
    }

    @SuppressWarnings("unchecked")
    private AgentProperties mapToAgentProperties(Map<String, Object> agent) {
        String timezone = (String) agent.get("timezone");
        List<String> origins = (List<String>) agent.get("origins");
        List<String> destinations = (List<String>) agent.get("destinations");

        Map<String, Object> searchMap = (Map<String, Object>) agent.get("search");
        AgentProperties.Search search = new AgentProperties.Search(
                (Integer) searchMap.get("horizonDays"),
                (List<Integer>) searchMap.get("fullDaysAllowed")
        );

        Map<String, Object> saturdayRuleMap = (Map<String, Object>) agent.get("saturdayRule");
        AgentProperties.SaturdayRule saturdayRule = new AgentProperties.SaturdayRule(
                (Boolean) saturdayRuleMap.get("requireNoFlightOnSaturday"),
                LocalTime.parse((String) saturdayRuleMap.get("latestArrivalOnFridayLocal")),
                LocalTime.parse((String) saturdayRuleMap.get("earliestDepartureOnSundayLocal"))
        );

        Map<String, Object> constraintsMap = (Map<String, Object>) agent.get("constraints");
        AgentProperties.Constraints constraints = new AgentProperties.Constraints(
                (Integer) constraintsMap.get("maxStops"),
                (Integer) constraintsMap.get("maxTotalDurationMinutesOneWay"),
                (Integer) constraintsMap.get("hardCapPricePLN"),
                LocalTime.parse((String) constraintsMap.get("earliestDepartureFromOriginLocal")),
                LocalTime.parse((String) constraintsMap.get("latestArrivalToOriginLocal"))
        );

        Map<String, Object> baselineMap = (Map<String, Object>) agent.get("baseline");
        Map<String, Object> segmentKeyMap = (Map<String, Object>) baselineMap.get("segmentKey");
        AgentProperties.Baseline.SegmentKey segmentKey = new AgentProperties.Baseline.SegmentKey(
                (Boolean) segmentKeyMap.get("includeDepartureMonth"),
                (Boolean) segmentKeyMap.get("includeFullDays")
        );
        AgentProperties.Baseline baseline = new AgentProperties.Baseline(
                (Integer) baselineMap.get("rollingDays"),
                segmentKey
        );

        Map<String, Object> candidateFilterMap = (Map<String, Object>) agent.get("candidateFilter");
        AgentProperties.CandidateFilter candidateFilter = new AgentProperties.CandidateFilter(
                (Integer) candidateFilterMap.get("minAbsoluteSavingPLN"),
                (Integer) candidateFilterMap.get("minPercentBelowMedian"),
                (Integer) candidateFilterMap.get("maxCandidatesPerRun")
        );

        Map<String, Object> alertsMap = (Map<String, Object>) agent.get("alerts");
        AgentProperties.Alerts alerts = new AgentProperties.Alerts(
                (String) alertsMap.get("channel"),
                (Integer) alertsMap.get("maxAlertsPerDay"),
                (Integer) alertsMap.get("maxAlertsPerDestinationPerWeek")
        );

        return new AgentProperties(
                timezone,
                origins,
                destinations,
                search,
                saturdayRule,
                constraints,
                baseline,
                candidateFilter,
                alerts
        );
    }
}

