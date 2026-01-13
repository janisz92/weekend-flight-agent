package pl.weekendflyer.weekendFlightAgent.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AgentPropertiesPlannerTest {

    private AgentProperties agentProperties;

    @BeforeEach
    void setUp() throws IOException {
        AgentPropertiesLoader loader = new AgentPropertiesLoader();
        agentProperties = loader.agentProperties();
    }

    @Test
    void plannerPropertiesShouldBeLoaded() {
        assertNotNull(agentProperties.planner());
    }

    @Test
    void maxWindowsPerDestinationPerDepartDateShouldBe3() {
        assertEquals(3, agentProperties.planner().maxWindowsPerDestinationPerDepartDate());
    }

    @Test
    void maxWindowsGlobalShouldBe500() {
        assertEquals(500, agentProperties.planner().maxWindowsGlobal());
    }

    @Test
    void minRecheckIntervalHoursShouldBe12() {
        assertEquals(12, agentProperties.planner().minRecheckIntervalHours());
    }

    @Test
    void dailyBudgetPerProviderShouldBe100() {
        assertEquals(100, agentProperties.planner().dailyBudgetPerProvider());
    }

    @Test
    void allPlannerFieldsShouldHaveExpectedValues() {
        AgentProperties.Planner planner = agentProperties.planner();

        assertAll(
                () -> assertEquals(3, planner.maxWindowsPerDestinationPerDepartDate()),
                () -> assertEquals(500, planner.maxWindowsGlobal()),
                () -> assertEquals(12, planner.minRecheckIntervalHours()),
                () -> assertEquals(100, planner.dailyBudgetPerProvider())
        );
    }
}

