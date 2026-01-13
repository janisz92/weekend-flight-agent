package pl.weekendflyer.weekendFlightAgent.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.weekendflyer.weekendFlightAgent.domain.repository.WindowCheckRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest
class DailyScanJobIntegrationTest {

    @Autowired
    private DailyScanJob dailyScanJob;

    @MockitoSpyBean
    private WindowCheckRepository windowCheckRepository;

    @Test
    void shouldRunDailyScanWithoutExceptions() {
        assertDoesNotThrow(() -> dailyScanJob.runDailyScan());
    }

    @Test
    void shouldUseRepositoryDuringDailyScan() {
        dailyScanJob.runDailyScan();

        verify(windowCheckRepository, atLeastOnce()).findByProviderAndWindowKeyIn(
                org.mockito.ArgumentMatchers.eq("default"),
                anyList()
        );
    }
}

