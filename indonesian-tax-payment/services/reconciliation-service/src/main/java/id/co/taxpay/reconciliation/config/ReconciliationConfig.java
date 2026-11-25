package id.co.taxpay.reconciliation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
public class ReconciliationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
