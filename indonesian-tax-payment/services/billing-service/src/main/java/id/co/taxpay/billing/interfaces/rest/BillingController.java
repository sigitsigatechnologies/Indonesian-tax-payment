package id.co.taxpay.billing.interfaces.rest;

import id.co.taxpay.billing.application.BillingApplicationService;
import id.co.taxpay.billing.application.model.BillingValidationResult;
import id.co.taxpay.billing.application.model.BillingView;
import id.co.taxpay.billing.interfaces.rest.dto.BillingValidationResponse;
import id.co.taxpay.billing.interfaces.rest.dto.ValidateBillingRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tax/billings")
@RequiredArgsConstructor
@Tag(name = "Tax Billings", description = "Tax billing inquiry and validation")
public class BillingController {

    private final BillingApplicationService billingApplicationService;

    @GetMapping("/{billingId}")
    @Operation(summary = "Inquire a tax billing")
    public BillingValidationResponse.Billing getBilling(@PathVariable String billingId) {
        BillingView view = billingApplicationService.getBilling(billingId);
        return BillingValidationResponse.Billing.from(view);
    }

    @PostMapping("/{billingId}/validate")
    @Operation(summary = "Validate a tax billing for payment")
    public BillingValidationResponse validate(@PathVariable String billingId,
                                              @Valid @RequestBody(required = false) ValidateBillingRequest request) {
        ValidateBillingRequest safeRequest = request == null
                ? new ValidateBillingRequest(null, null, null)
                : request;
        BillingValidationResult result = billingApplicationService.validate(
                billingId, safeRequest.amount(), safeRequest.currency(), safeRequest.taxpayerId());
        return BillingValidationResponse.from(result);
    }
}
