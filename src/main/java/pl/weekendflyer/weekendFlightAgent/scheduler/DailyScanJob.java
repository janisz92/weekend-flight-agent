package pl.weekendflyer.weekendFlightAgent.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.weekendflyer.weekendFlightAgent.config.AgentProperties;
import pl.weekendflyer.weekendFlightAgent.domain.model.CandidateWindow;
import pl.weekendflyer.weekendFlightAgent.domain.model.PlannerResult;
import pl.weekendflyer.weekendFlightAgent.domain.planner.TripWindowGenerator;
import pl.weekendflyer.weekendFlightAgent.domain.planner.WindowCheckPlanner;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyScanJob {

    private static final String DEFAULT_PROVIDER = "default";

    private final AgentProperties props;
    private final TripWindowGenerator tripWindowGenerator;
    private final WindowCheckPlanner windowCheckPlanner;

    @Scheduled(cron = "0 10 7 * * *", zone = "${agent.timezone:Europe/Warsaw}")
    public void runDailyScan() {
        log.info("Daily scan started - origins={}, destinations={}, horizonDays={}",
                props.origins(),
                props.destinations().size(),
                props.search().horizonDays()
        );

        List<CandidateWindow> candidates = tripWindowGenerator.generate(
                props.origins(),
                props.destinations(),
                props.search().horizonDays(),
                props.search().fullDaysAllowed()
        );

        log.info("Generated {} candidate windows", candidates.size());

        PlannerResult result = windowCheckPlanner.plan(DEFAULT_PROVIDER, candidates);

        log.info("Planner result: totalCandidates={}, selectedCount={}, skippedRecentlyChecked={}, skippedBudget={}",
                result.totalCandidates(),
                result.selectedCount(),
                result.skippedRecentlyChecked(),
                result.skippedBudget()
        );

        logSelectedPerDestination(result.selected());

        log.info("Daily scan finished.");
    }

    private void logSelectedPerDestination(List<CandidateWindow> selected) {
        Map<String, Long> countPerDestination = selected.stream()
                .collect(Collectors.groupingBy(CandidateWindow::destination, Collectors.counting()));

        countPerDestination.forEach((destination, count) ->
                log.debug("Selected for {}: {} windows", destination, count));

        if (!countPerDestination.isEmpty()) {
            log.info("Selected windows per destination: {}", countPerDestination);
        }
    }
}
