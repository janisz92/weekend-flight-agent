package pl.weekendflyer.weekendFlightAgent.domain.planner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.weekendflyer.weekendFlightAgent.domain.model.CandidateWindow;
import pl.weekendflyer.weekendFlightAgent.domain.model.PlannerResult;
import pl.weekendflyer.weekendFlightAgent.domain.model.WindowCheck;
import pl.weekendflyer.weekendFlightAgent.domain.repository.WindowCheckRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WindowCheckPlannerTest {

    private static final ZoneId WARSAW_ZONE = ZoneId.of("Europe/Warsaw");
    private static final String PROVIDER = "TestProvider";

    @Mock
    private WindowCheckRepository repository;

    @Captor
    private ArgumentCaptor<List<WindowCheck>> saveCaptor;

    private Clock fixedClock;
    private Instant fixedNow;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.of(2026, 1, 14);
        fixedNow = today.atStartOfDay(WARSAW_ZONE).toInstant();
        fixedClock = Clock.fixed(fixedNow, WARSAW_ZONE);
    }

    @Test
    void shouldReturnNewWindowsFirst() {
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, 10, repository);

        CandidateWindow newWindow = createCandidate("WAW", "LIS", today.plusDays(2), today.plusDays(4));
        CandidateWindow existingWindow = createCandidate("WAW", "BCN", today.plusDays(2), today.plusDays(4));

        WindowCheck existingCheck = createWindowCheck(existingWindow, fixedNow.minusSeconds(86400), 5);

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(List.of(existingCheck));

        PlannerResult result = planner.plan(PROVIDER, List.of(existingWindow, newWindow));

        assertEquals(2, result.selectedCount());
        assertEquals(newWindow.windowKey(), result.selected().get(0).windowKey());
    }

    @Test
    void shouldFilterOutRecentlyCheckedWindows() {
        int minRecheckIntervalHours = 12;
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, minRecheckIntervalHours, 10, repository);

        CandidateWindow recentlyChecked = createCandidate("WAW", "LIS", today.plusDays(2), today.plusDays(4));
        CandidateWindow notRecentlyChecked = createCandidate("WAW", "BCN", today.plusDays(2), today.plusDays(4));

        Instant twoHoursAgo = fixedNow.minusSeconds(2 * 3600);
        Instant twentyHoursAgo = fixedNow.minusSeconds(20 * 3600);

        WindowCheck recentCheck = createWindowCheck(recentlyChecked, twoHoursAgo, 1);
        WindowCheck oldCheck = createWindowCheck(notRecentlyChecked, twentyHoursAgo, 1);

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(List.of(recentCheck, oldCheck));

        PlannerResult result = planner.plan(PROVIDER, List.of(recentlyChecked, notRecentlyChecked));

        assertEquals(1, result.selectedCount());
        assertEquals(1, result.skippedRecentlyChecked());
        assertEquals(notRecentlyChecked.windowKey(), result.selected().get(0).windowKey());
    }

    @Test
    void shouldPrioritizeCloserDepartDates() {
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, 10, repository);

        CandidateWindow farWindow = createCandidate("WAW", "LIS", today.plusDays(10), today.plusDays(12));
        CandidateWindow closeWindow = createCandidate("WAW", "BCN", today.plusDays(2), today.plusDays(4));

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(Collections.emptyList());

        PlannerResult result = planner.plan(PROVIDER, List.of(farWindow, closeWindow));

        assertEquals(2, result.selectedCount());
        assertEquals(closeWindow.windowKey(), result.selected().get(0).windowKey());
        assertEquals(farWindow.windowKey(), result.selected().get(1).windowKey());
    }

    @Test
    void shouldPrioritizeLessCheckedWindows() {
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, 10, repository);

        CandidateWindow manyChecks = createCandidate("WAW", "LIS", today.plusDays(2), today.plusDays(4));
        CandidateWindow fewChecks = createCandidate("WAW", "BCN", today.plusDays(2), today.plusDays(4));

        Instant oldTime = fixedNow.minusSeconds(24 * 3600);

        WindowCheck manyChecksRecord = createWindowCheck(manyChecks, oldTime, 10);
        WindowCheck fewChecksRecord = createWindowCheck(fewChecks, oldTime, 2);

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(List.of(manyChecksRecord, fewChecksRecord));

        PlannerResult result = planner.plan(PROVIDER, List.of(manyChecks, fewChecks));

        assertEquals(2, result.selectedCount());
        assertEquals(fewChecks.windowKey(), result.selected().get(0).windowKey());
        assertEquals(manyChecks.windowKey(), result.selected().get(1).windowKey());
    }

    @Test
    void shouldRespectDailyBudget() {
        int dailyBudget = 2;
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, dailyBudget, repository);

        List<CandidateWindow> candidates = List.of(
                createCandidate("WAW", "LIS", today.plusDays(2), today.plusDays(4)),
                createCandidate("WAW", "BCN", today.plusDays(3), today.plusDays(5)),
                createCandidate("WAW", "MAD", today.plusDays(4), today.plusDays(6)),
                createCandidate("WAW", "FCO", today.plusDays(5), today.plusDays(7))
        );

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(Collections.emptyList());

        PlannerResult result = planner.plan(PROVIDER, candidates);

        assertEquals(dailyBudget, result.selectedCount());
        assertEquals(dailyBudget, result.selected().size());
        assertEquals(2, result.skippedBudget());
        assertEquals(4, result.totalCandidates());
    }

    @Test
    void shouldPersistWindowChecksOnPlan() {
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, 10, repository);

        CandidateWindow newWindow = createCandidate("WAW", "LIS", today.plusDays(2), today.plusDays(4));
        CandidateWindow existingWindow = createCandidate("WAW", "BCN", today.plusDays(3), today.plusDays(5));

        Instant oldTime = fixedNow.minusSeconds(24 * 3600);
        WindowCheck existingCheck = createWindowCheck(existingWindow, oldTime, 3);

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(List.of(existingCheck));

        planner.plan(PROVIDER, List.of(newWindow, existingWindow));

        verify(repository).saveAll(saveCaptor.capture());

        List<WindowCheck> saved = saveCaptor.getValue();
        assertEquals(2, saved.size());

        WindowCheck savedNew = saved.stream()
                .filter(c -> c.getWindowKey().equals(newWindow.windowKey()))
                .findFirst()
                .orElseThrow();

        assertEquals(1, savedNew.getCheckCount());
        assertEquals(fixedNow, savedNew.getLastCheckedAt());
        assertEquals(PROVIDER, savedNew.getProvider());
        assertEquals("WAW", savedNew.getOrigin());
        assertEquals("LIS", savedNew.getDestination());

        WindowCheck savedExisting = saved.stream()
                .filter(c -> c.getWindowKey().equals(existingWindow.windowKey()))
                .findFirst()
                .orElseThrow();

        assertEquals(4, savedExisting.getCheckCount());
        assertEquals(fixedNow, savedExisting.getLastCheckedAt());
    }

    @Test
    void shouldUseClockForNow() {
        Instant specificInstant = Instant.parse("2026-01-14T10:30:00Z");
        Clock specificClock = Clock.fixed(specificInstant, WARSAW_ZONE);
        WindowCheckPlanner planner = new WindowCheckPlanner(specificClock, 12, 10, repository);

        CandidateWindow candidate = createCandidate("WAW", "LIS", today.plusDays(2), today.plusDays(4));

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(Collections.emptyList());

        planner.plan(PROVIDER, List.of(candidate));

        verify(repository).saveAll(saveCaptor.capture());

        WindowCheck saved = saveCaptor.getValue().get(0);
        assertEquals(specificInstant, saved.getLastCheckedAt());
    }

    @Test
    void shouldReturnEmptyResultForEmptyCandidates() {
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, 10, repository);

        PlannerResult result = planner.plan(PROVIDER, Collections.emptyList());

        assertEquals(0, result.selectedCount());
        assertEquals(0, result.totalCandidates());
        assertTrue(result.selected().isEmpty());
        verifyNoInteractions(repository);
    }

    @Test
    void shouldHandleWindowWithNullLastCheckedAt() {
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, 10, repository);

        CandidateWindow candidate = createCandidate("WAW", "LIS", today.plusDays(2), today.plusDays(4));

        WindowCheck checkWithNullLastChecked = createWindowCheck(candidate, null, 0);

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(List.of(checkWithNullLastChecked));

        PlannerResult result = planner.plan(PROVIDER, List.of(candidate));

        assertEquals(1, result.selectedCount());
        assertEquals(0, result.skippedRecentlyChecked());
    }

    @Test
    void shouldSortDeterministicallyByWindowKeyAsTieBreaker() {
        WindowCheckPlanner planner = new WindowCheckPlanner(fixedClock, 12, 10, repository);

        CandidateWindow windowA = createCandidate("WAW", "AAA", today.plusDays(2), today.plusDays(4));
        CandidateWindow windowB = createCandidate("WAW", "BBB", today.plusDays(2), today.plusDays(4));
        CandidateWindow windowC = createCandidate("WAW", "CCC", today.plusDays(2), today.plusDays(4));

        when(repository.findByProviderAndWindowKeyIn(eq(PROVIDER), anyList()))
                .thenReturn(Collections.emptyList());

        PlannerResult result = planner.plan(PROVIDER, List.of(windowC, windowA, windowB));

        assertEquals(3, result.selectedCount());
        assertEquals(windowA.windowKey(), result.selected().get(0).windowKey());
        assertEquals(windowB.windowKey(), result.selected().get(1).windowKey());
        assertEquals(windowC.windowKey(), result.selected().get(2).windowKey());
    }

    private CandidateWindow createCandidate(String origin, String destination, LocalDate departDate, LocalDate returnDate) {
        return new CandidateWindow(origin, destination, departDate, returnDate);
    }

    private WindowCheck createWindowCheck(CandidateWindow candidate, Instant lastCheckedAt, int checkCount) {
        WindowCheck check = new WindowCheck();
        check.setId((long) candidate.windowKey().hashCode());
        check.setProvider(PROVIDER);
        check.setOrigin(candidate.origin());
        check.setDestination(candidate.destination());
        check.setDepartDate(candidate.departDate());
        check.setReturnDate(candidate.returnDate());
        check.setWindowKey(candidate.windowKey());
        check.setLastCheckedAt(lastCheckedAt);
        check.setCheckCount(checkCount);
        check.setCreatedAt(Instant.now());
        check.setUpdatedAt(Instant.now());
        return check;
    }
}

