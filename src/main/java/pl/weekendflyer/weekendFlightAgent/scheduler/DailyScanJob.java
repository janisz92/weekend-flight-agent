package pl.weekendflyer.weekendFlightAgent.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.weekendflyer.weekendFlightAgent.config.AgentProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyScanJob {

    private final AgentProperties props;

    @Scheduled(cron = "0 10 7 * * *", zone = "Europe/Warsaw")
    public void runDailyScan() {
        log.info("Daily scan started.");
        log.info("Config loaded: tz={}, origins={}, destinations={}, horizonDays={}, fullDaysAllowed={}",
                props.timezone(),
                props.origins(),
                props.destinations().size(),
                props.search().horizonDays(),
                props.search().fullDaysAllowed()
        );

        log.info("Stage 1 dry-run: next steps later -> generate trip windows, call provider, persist results.");
        log.info("Daily scan finished.");
    }
}
