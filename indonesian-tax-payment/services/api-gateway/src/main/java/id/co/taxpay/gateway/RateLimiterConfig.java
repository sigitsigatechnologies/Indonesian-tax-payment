package id.co.taxpay.gateway;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver customerKeyResolver() {
        return exchange -> {
            String customerId = exchange.getRequest().getHeaders().getFirst("X-Customer-ID");
            if (customerId != null && !customerId.isBlank()) {
                return Mono.just(customerId);
            }
            String remoteAddress = Objects.requireNonNull(exchange.getRequest().getRemoteAddress())
                    .getAddress().getHostAddress();
            return Mono.just(remoteAddress);
        };
    }
}
