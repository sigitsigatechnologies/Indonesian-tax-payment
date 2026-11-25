package id.co.taxpay.payment.infrastructure.clients;

import id.co.taxpay.payment.infrastructure.clients.dto.BillingValidationResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.ValidateBillingRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "billing-service", url = "${services.billing.url:http://localhost:8083}")
public interface BillingClient {

    @PostMapping("/api/v1/tax/billings/{billingId}/validate")
    BillingValidationResponseDto validate(@PathVariable("billingId") String billingId,
                                          @RequestBody ValidateBillingRequestDto request);
}
