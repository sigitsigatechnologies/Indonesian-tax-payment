package id.co.taxpay.corebanking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coreBankingOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Core Banking Mock API")
                .version("v1")
                .description("MOCK ONLY - simulated bank account and transaction inquiry. No real bank data."));
    }
}
