package pl.weekendflyer.weekendFlightAgent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.weekendflyer.weekendFlightAgent.domain.planner.TripWindowGenerator;
import pl.weekendflyer.weekendFlightAgent.domain.planner.WindowCheckPlanner;
import pl.weekendflyer.weekendFlightAgent.domain.repository.WindowCheckRepository;

import java.time.Clock;

@Configuration
public class PlannerConfig {

    @Bean
    public TripWindowGenerator tripWindowGenerator(Clock clock, AgentProperties agentProperties) {
        return new TripWindowGenerator(clock, agentProperties.planner());
    }

    @Bean
    public WindowCheckPlanner windowCheckPlanner(Clock clock, AgentProperties agentProperties, WindowCheckRepository repository) {
        return new WindowCheckPlanner(clock, agentProperties.planner(), repository);
    }
}

