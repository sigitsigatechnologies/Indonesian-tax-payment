package id.co.taxpay.taxgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TaxGatewayConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
