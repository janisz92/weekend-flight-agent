package pl.weekendflyer.weekendFlightAgent.domain.model;

import java.util.List;

public record PlannerResult(
        List<CandidateWindow> selected,
        int totalCandidates,
        int skippedRecentlyChecked,
        int skippedBudget,
        int selectedCount
) {

    public PlannerResult {
        if (selected == null) {
            throw new IllegalArgumentException("selected list must be non-null");
        }
        selected = List.copyOf(selected);
    }

    public static PlannerResult empty() {
        return new PlannerResult(List.of(), 0, 0, 0, 0);
    }
}

