package pl.weekendflyer.weekendFlightAgent.domain.eval;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.weekendflyer.weekendFlightAgent.config.AgentProperties;
import pl.weekendflyer.weekendFlightAgent.domain.model.TripConstraints;

/**
 * Fabryka tworząca TripConstraints na podstawie AgentProperties.
 * Mapuje konfigurację agenta na twarde ograniczenia dla wyszukiwania lotów.
 */
@Component
@RequiredArgsConstructor
public class TripConstraintsFactory {

    private final AgentProperties agentProperties;

    /**
     * Zwraca aktualne ograniczenia dla wyszukiwania lotów na podstawie konfiguracji.
     *
     * @return TripConstraints zbudowane z AgentProperties
     */
    public TripConstraints current() {
        return new TripConstraints(
                agentProperties.constraints().maxStops(),
                agentProperties.constraints().maxTotalDurationMinutesOneWay(),
                agentProperties.constraints().hardCapPricePLN(),
                agentProperties.saturdayRule().latestArrivalOnFridayLocal(),
                agentProperties.saturdayRule().earliestDepartureOnSundayLocal(),
                agentProperties.saturdayRule().requireNoFlightOnSaturday()
        );
    }
}

