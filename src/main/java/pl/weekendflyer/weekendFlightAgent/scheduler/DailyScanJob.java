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

    @Scheduled(cron = "0 10 7 * * *", zone = "${agent.timezone:Europe/Warsaw}")
    public void runDailyScan() {
        log.info("Daily scan started - origins={}, destinations={}, horizonDays={}",
                props.origins(),
                props.destinations().size(),
                props.search().horizonDays()
        );
        log.info("Daily scan finished.");
    }
}
