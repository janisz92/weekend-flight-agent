package pl.weekendflyer.weekendFlightAgent.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DailyScanJob {

    private static final Logger log =
            LoggerFactory.getLogger(DailyScanJob.class);

    @Scheduled(fixedDelay = 60000)
    public void run() {
        log.info("DailyScanJob dry-run OK");
    }
}
