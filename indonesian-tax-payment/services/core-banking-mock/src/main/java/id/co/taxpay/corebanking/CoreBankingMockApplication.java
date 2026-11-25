package id.co.taxpay.corebanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"id.co.taxpay.corebanking", "id.co.taxpay.common.web", "id.co.taxpay.common.security"})
public class CoreBankingMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreBankingMockApplication.class, args);
    }
}
