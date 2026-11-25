package id.co.taxpay.payment.interfaces.rest;

import id.co.taxpay.payment.application.command.InitiatePaymentCommand;
import id.co.taxpay.payment.application.service.PaymentApplicationService;
import id.co.taxpay.payment.domain.exception.MissingIdempotencyKeyException;
import id.co.taxpay.payment.interfaces.rest.dto.ApprovalRequest;
import id.co.taxpay.payment.interfaces.rest.dto.InitiatePaymentRequest;
import id.co.taxpay.payment.interfaces.rest.dto.PageResponse;
import id.co.taxpay.payment.interfaces.rest.dto.ReverseRequest;
import id.co.taxpay.payment.interfaces.rest.dto.PaymentHistoryResponse;
import id.co.taxpay.payment.interfaces.rest.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tax/payments")
@RequiredArgsConstructor
@Validated
@Tag(name = "Tax Payments", description = "Corporate tax payment lifecycle")
public class PaymentController {

    private final PaymentApplicationService paymentApplicationService;

    @PostMapping
    @Operation(summary = "Initiate a tax payment")
    public ResponseEntity<PaymentResponse> initiate(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Actor", defaultValue = "cms-user") String actor,
            @Valid @RequestBody InitiatePaymentRequest request) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException();
        }
        InitiatePaymentCommand command = new InitiatePaymentCommand(
                request.billingId(),
                request.debitAccountNumber(),
                request.amount(),
                request.currency(),
                request.customerId()
        );
        PaymentResponse response = PaymentResponse.from(
                paymentApplicationService.initiate(command, idempotencyKey.trim(), actor));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{paymentId}/approve")
    @PreAuthorize("hasAuthority('SCOPE_CHECKER')")
    @Operation(summary = "Approve a payment as checker")
    public PaymentResponse approve(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-Actor", defaultValue = "checker-user") String actor,
            @RequestHeader(value = "X-Role", required = false) String role,
            @Valid @RequestBody(required = false) ApprovalRequest request) {
        String notes = request == null ? null : request.notes();
        return PaymentResponse.from(paymentApplicationService.approve(paymentId, actor, role, notes));
    }

    @PostMapping("/{paymentId}/reject")
    @PreAuthorize("hasAuthority('SCOPE_CHECKER')")
    @Operation(summary = "Reject a payment as checker")
    public PaymentResponse reject(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-Actor", defaultValue = "checker-user") String actor,
            @RequestHeader(value = "X-Role", required = false) String role,
            @Valid @RequestBody(required = false) ApprovalRequest request) {
        String notes = request == null ? null : request.notes();
        return PaymentResponse.from(paymentApplicationService.reject(paymentId, actor, role, notes));
    }

    @PostMapping("/{paymentId}/reverse")
    @PreAuthorize("hasAuthority('SCOPE_CHECKER')")
    @Operation(summary = "Reverse a payment after a confirmed failure or success")
    public PaymentResponse reverse(
            @PathVariable UUID paymentId,
            @RequestHeader(value = "X-Actor", defaultValue = "checker-user") String actor,
            @Valid @RequestBody ReverseRequest request) {
        return PaymentResponse.from(
                paymentApplicationService.reverse(paymentId, request.reason(), actor));
    }

    @GetMapping("/{paymentId}")
    @Operation(summary = "Get a payment by id")
    public PaymentResponse getById(@PathVariable UUID paymentId) {
        return PaymentResponse.from(paymentApplicationService.getById(paymentId));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get a payment by payment reference")
    public PaymentResponse getByReference(@PathVariable @NotBlank String reference) {
        return PaymentResponse.from(paymentApplicationService.getByReference(reference));
    }

    @GetMapping("/{paymentId}/history")
    @Operation(summary = "Get the immutable status history of a payment")
    public List<PaymentHistoryResponse> history(@PathVariable UUID paymentId) {
        return paymentApplicationService.getHistory(paymentId).stream()
                .map(PaymentHistoryResponse::from)
                .toList();
    }

    @GetMapping
    @Operation(summary = "List payments for a customer")
    public PageResponse<PaymentResponse> list(
            @RequestParam @NotBlank String customerId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return PageResponse.from(
                paymentApplicationService.listByCustomer(customerId, page, size),
                PaymentResponse::from
        );
    }
}
