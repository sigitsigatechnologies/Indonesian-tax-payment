package id.co.taxpay.payment.infrastructure.clients;

import id.co.taxpay.payment.infrastructure.clients.dto.GovernmentPaymentResponseDto;
import id.co.taxpay.payment.infrastructure.clients.dto.SubmitPaymentRequestDto;
import id.co.taxpay.payment.infrastructure.clients.dto.SubmitPaymentResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "tax-gateway-mock", url = "${services.tax-gateway.url:http://localhost:8085}")
public interface TaxGatewayClient {

    @PostMapping("/api/v1/government/payments")
    SubmitPaymentResponseDto submit(@RequestBody SubmitPaymentRequestDto request,
                                    @RequestHeader("X-Mock-Scenario") String scenario);

    @GetMapping("/api/v1/government/payments/{reference}")
    GovernmentPaymentResponseDto getPayment(@PathVariable("reference") String reference);
}
