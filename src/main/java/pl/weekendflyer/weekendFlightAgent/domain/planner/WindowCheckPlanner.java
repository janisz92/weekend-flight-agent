package pl.weekendflyer.weekendFlightAgent.domain.planner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.weekendflyer.weekendFlightAgent.config.AgentProperties;
import pl.weekendflyer.weekendFlightAgent.domain.model.CandidateWindow;
import pl.weekendflyer.weekendFlightAgent.domain.model.PlannerResult;
import pl.weekendflyer.weekendFlightAgent.domain.model.WindowCheck;
import pl.weekendflyer.weekendFlightAgent.domain.repository.WindowCheckRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class WindowCheckPlanner {

    private final Clock clock;
    private final int minRecheckIntervalHours;
    private final int dailyBudgetPerProvider;
    private final WindowCheckRepository repository;

    public WindowCheckPlanner(Clock clock, AgentProperties.Planner plannerConfig, WindowCheckRepository repository) {
        this(clock, plannerConfig.minRecheckIntervalHours(), plannerConfig.dailyBudgetPerProvider(), repository);
    }

    public PlannerResult plan(String provider, List<CandidateWindow> candidates) {
        if (candidates.isEmpty()) {
            return PlannerResult.empty();
        }

        Instant now = Instant.now(clock);
        LocalDate today = LocalDate.now(clock);

        List<String> windowKeys = candidates.stream()
                .map(CandidateWindow::windowKey)
                .toList();

        List<WindowCheck> existingChecks = repository.findByProviderAndWindowKeyIn(provider, windowKeys);
        Map<String, WindowCheck> checksByKey = existingChecks.stream()
                .collect(Collectors.toMap(WindowCheck::getWindowKey, Function.identity()));

        int skippedRecentlyChecked = 0;
        List<CandidateWindow> eligibleCandidates = new ArrayList<>();

        for (CandidateWindow candidate : candidates) {
            WindowCheck existing = checksByKey.get(candidate.windowKey());

            if (existing != null && existing.getLastCheckedAt() != null) {
                Instant recheckThreshold = existing.getLastCheckedAt().plus(minRecheckIntervalHours, ChronoUnit.HOURS);
                if (recheckThreshold.isAfter(now)) {
                    skippedRecentlyChecked++;
                    continue;
                }
            }

            eligibleCandidates.add(candidate);
        }

        eligibleCandidates.sort(buildPriorityComparator(checksByKey, today));

        int selectedCount = Math.min(eligibleCandidates.size(), dailyBudgetPerProvider);
        int skippedBudget = Math.max(0, eligibleCandidates.size() - dailyBudgetPerProvider);

        List<CandidateWindow> selected = eligibleCandidates.subList(0, selectedCount);

        List<WindowCheck> toSave = new ArrayList<>();
        for (CandidateWindow candidate : selected) {
            WindowCheck check = checksByKey.get(candidate.windowKey());

            if (check == null) {
                check = createNewWindowCheck(provider, candidate, now);
            } else {
                check.setLastCheckedAt(now);
                check.setCheckCount(check.getCheckCount() + 1);
            }

            toSave.add(check);
        }

        if (!toSave.isEmpty()) {
            repository.saveAll(toSave);
        }

        log.info("WindowCheckPlanner: provider={}, candidates={}, eligible={}, selected={}, skippedRecent={}, skippedBudget={}",
                provider, candidates.size(), eligibleCandidates.size(), selectedCount, skippedRecentlyChecked, skippedBudget);

        return new PlannerResult(
                selected,
                candidates.size(),
                skippedRecentlyChecked,
                skippedBudget,
                selectedCount
        );
    }

    private Comparator<CandidateWindow> buildPriorityComparator(Map<String, WindowCheck> checksByKey, LocalDate today) {
        return Comparator
                .comparing((CandidateWindow c) -> {
                    WindowCheck check = checksByKey.get(c.windowKey());
                    return check != null && check.getLastCheckedAt() != null ? 1 : 0;
                })
                .thenComparing(c -> ChronoUnit.DAYS.between(today, c.departDate()))
                .thenComparing(c -> {
                    WindowCheck check = checksByKey.get(c.windowKey());
                    return check != null ? check.getCheckCount() : 0;
                })
                .thenComparing(CandidateWindow::windowKey);
    }

    private WindowCheck createNewWindowCheck(String provider, CandidateWindow candidate, Instant now) {
        WindowCheck check = new WindowCheck();
        check.setProvider(provider);
        check.setOrigin(candidate.origin());
        check.setDestination(candidate.destination());
        check.setDepartDate(candidate.departDate());
        check.setReturnDate(candidate.returnDate());
        check.setWindowKey(candidate.windowKey());
        check.setLastCheckedAt(now);
        check.setCheckCount(1);
        return check;
    }
}

