package pl.weekendflyer.weekendFlightAgent.domain.planner;

import lombok.extern.slf4j.Slf4j;
import pl.weekendflyer.weekendFlightAgent.config.AgentProperties;
import pl.weekendflyer.weekendFlightAgent.domain.model.CandidateWindow;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class TripWindowGenerator {

    private final Clock clock;
    private final int maxWindowsPerDestinationPerDepartDate;
    private final int maxWindowsGlobal;

    public TripWindowGenerator(Clock clock, AgentProperties.Planner plannerConfig) {
        this(clock, plannerConfig.maxWindowsPerDestinationPerDepartDate(), plannerConfig.maxWindowsGlobal());
    }

    public TripWindowGenerator(Clock clock, int maxWindowsPerDestinationPerDepartDate, int maxWindowsGlobal) {
        this.clock = clock;
        this.maxWindowsPerDestinationPerDepartDate = maxWindowsPerDestinationPerDepartDate;
        this.maxWindowsGlobal = maxWindowsGlobal;
    }

    public List<CandidateWindow> generate(
            List<String> origins,
            List<String> destinations,
            int horizonDays,
            List<Integer> fullDaysAllowed
    ) {
        LocalDate today = LocalDate.now(clock);
        LocalDate horizonEnd = today.plusDays(horizonDays);

        List<CandidateWindow> allCandidates = new ArrayList<>();
        Set<String> seenWindowKeys = new HashSet<>();
        Map<String, Integer> countPerDestinationDepartDate = new HashMap<>();
        Map<String, Integer> countPerDestination = new HashMap<>();

        for (String origin : origins) {
            for (String destination : destinations) {
                for (LocalDate departDate = today; departDate.isBefore(horizonEnd); departDate = departDate.plusDays(1)) {
                    List<CandidateWindow> windowsForDestDepartDate = new ArrayList<>();

                    for (Integer fullDays : fullDaysAllowed) {
                        LocalDate returnDate = departDate.plusDays(fullDays + 1);

                        if (returnDate.isAfter(horizonEnd)) {
                            continue;
                        }

                        String windowKey = WindowKeyGenerator.generate(origin, destination, departDate, returnDate);
                        if (seenWindowKeys.contains(windowKey)) {
                            continue;
                        }

                        try {
                            CandidateWindow candidate = new CandidateWindow(origin, destination, departDate, returnDate);

                            if (!candidate.hasSaturdayInMiddle()) {
                                continue;
                            }

                            windowsForDestDepartDate.add(candidate);
                            seenWindowKeys.add(windowKey);
                        } catch (IllegalArgumentException e) {
                            log.debug("Skipping invalid window: {}-{} {} to {}: {}",
                                    origin, destination, departDate, returnDate, e.getMessage());
                        }
                    }

                    windowsForDestDepartDate.sort(Comparator.comparing(CandidateWindow::returnDate));

                    String destDepartKey = destination + "-" + departDate;
                    int currentCount = countPerDestinationDepartDate.getOrDefault(destDepartKey, 0);
                    int remaining = maxWindowsPerDestinationPerDepartDate - currentCount;

                    for (int i = 0; i < Math.min(windowsForDestDepartDate.size(), remaining); i++) {
                        allCandidates.add(windowsForDestDepartDate.get(i));
                        countPerDestinationDepartDate.merge(destDepartKey, 1, Integer::sum);
                        countPerDestination.merge(destination, 1, Integer::sum);
                    }
                }
            }
        }

        allCandidates.sort(Comparator
                .comparing(CandidateWindow::origin)
                .thenComparing(CandidateWindow::destination)
                .thenComparing(CandidateWindow::departDate)
                .thenComparing(CandidateWindow::returnDate));

        List<CandidateWindow> result = allCandidates.size() > maxWindowsGlobal
                ? allCandidates.subList(0, maxWindowsGlobal)
                : allCandidates;

        log.info("Generated {} candidate windows (global limit: {})", result.size(), maxWindowsGlobal);
        countPerDestination.forEach((dest, count) ->
                log.debug("Destination {}: {} windows", dest, count));

        return new ArrayList<>(result);
    }
}

