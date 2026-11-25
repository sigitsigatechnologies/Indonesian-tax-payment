package id.co.taxpay.taxgateway.interfaces.rest;

import id.co.taxpay.taxgateway.application.service.TaxGatewaySubmissionService;
import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentBillingRecordEntity;
import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentTaxPaymentEntity;
import id.co.taxpay.taxgateway.infrastructure.persistence.SpringDataGovernmentBillingRepository;
import id.co.taxpay.taxgateway.infrastructure.persistence.SpringDataGovernmentPaymentRepository;
import id.co.taxpay.taxgateway.interfaces.rest.dto.GovernmentBillingResponse;
import id.co.taxpay.taxgateway.interfaces.rest.dto.GovernmentPaymentResponse;
import id.co.taxpay.taxgateway.interfaces.rest.dto.SubmitPaymentRequest;
import id.co.taxpay.taxgateway.interfaces.rest.dto.SubmitPaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/government")
@RequiredArgsConstructor
@Tag(name = "Tax Gateway Mock", description = "Simulated DJP/MPN gateway. All references are mock data.")
public class TaxGatewayController {

    private final SpringDataGovernmentBillingRepository billingRepository;
    private final SpringDataGovernmentPaymentRepository paymentRepository;
    private final TaxGatewaySubmissionService submissionService;

    @GetMapping("/billings/{billingId}")
    @Operation(summary = "Inquire a government billing record")
    public GovernmentBillingResponse getBilling(@PathVariable String billingId) {
        GovernmentBillingRecordEntity entity = billingRepository.findByBillingId(billingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Government billing " + billingId + " was not found"));
        return GovernmentBillingResponse.from(entity);
    }

    @PostMapping("/payments")
    @Operation(summary = "Submit a government tax payment (mock)")
    public SubmitPaymentResponse submit(
            @Valid @RequestBody SubmitPaymentRequest request,
            @RequestHeader(value = "X-Mock-Scenario", defaultValue = "NONE") String scenario) {
        if ("TIMEOUT_BEFORE_SUBMIT".equalsIgnoreCase(scenario)) {
            sleep(3000);
        }
        SubmitPaymentResponse response = submissionService.submit(request, scenario);
        if ("TIMEOUT_AFTER_SUBMIT".equalsIgnoreCase(scenario)
                || "DELAYED".equalsIgnoreCase(scenario)) {
            sleep("DELAYED".equalsIgnoreCase(scenario) ? 2000 : 3000);
        }
        return response;
    }

    @GetMapping("/payments/{reference}")
    @Operation(summary = "Inquire a simulated government payment")
    public GovernmentPaymentResponse getPayment(@PathVariable String reference) {
        GovernmentTaxPaymentEntity entity = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Government payment " + reference + " was not found"));
        return GovernmentPaymentResponse.from(entity);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Simulated timeout interrupted");
        }
    }
}
