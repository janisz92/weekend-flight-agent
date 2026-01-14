package pl.weekendflyer.weekendFlightAgent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(AgentProperties agentProperties) {
        return Clock.system(ZoneId.of(agentProperties.timezone()));
    }
}

