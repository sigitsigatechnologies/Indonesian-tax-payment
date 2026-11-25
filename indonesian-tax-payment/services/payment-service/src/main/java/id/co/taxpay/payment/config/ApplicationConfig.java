package id.co.taxpay.payment.config;

import feign.RequestInterceptor;
import id.co.taxpay.common.security.JwtTokenService;
import id.co.taxpay.common.web.CorrelationIdHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableAsync
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }

    @Bean(name = "sagaTaskExecutor")
    public TaskExecutor sagaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("taxpay-saga-");
        executor.initialize();
        return executor;
    }

    @Bean
    public RequestInterceptor feignAuthenticationInterceptor(JwtTokenService tokenService) {
        return template -> {
            template.header("Authorization",
                    "Bearer " + tokenService.issue("payment-service", "SYSTEM", Duration.ofHours(1)));
            CorrelationIdHolder.get().ifPresent(id -> template.header("X-Correlation-ID", id));
        };
    }
}
