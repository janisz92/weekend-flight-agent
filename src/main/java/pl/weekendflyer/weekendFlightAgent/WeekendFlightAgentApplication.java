package pl.weekendflyer.weekendFlightAgent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.weekendflyer.weekendFlightAgent.config.AgentProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AgentProperties.class)
public class WeekendFlightAgentApplication {
	public static void main(String[] args) {
		SpringApplication.run(WeekendFlightAgentApplication.class, args);
	}

}
