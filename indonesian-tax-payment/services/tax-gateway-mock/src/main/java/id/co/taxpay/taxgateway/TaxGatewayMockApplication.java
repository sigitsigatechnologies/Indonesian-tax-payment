package id.co.taxpay.taxgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"id.co.taxpay.taxgateway", "id.co.taxpay.common.web", "id.co.taxpay.common.security"})
public class TaxGatewayMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxGatewayMockApplication.class, args);
    }
}
