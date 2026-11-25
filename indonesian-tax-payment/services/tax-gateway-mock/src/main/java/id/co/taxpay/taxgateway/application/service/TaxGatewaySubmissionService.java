package id.co.taxpay.taxgateway.application.service;

import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentBillingRecordEntity;
import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentPaymentResponseEntity;
import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentTaxPaymentEntity;
import id.co.taxpay.taxgateway.infrastructure.persistence.SpringDataGovernmentBillingRepository;
import id.co.taxpay.taxgateway.infrastructure.persistence.SpringDataGovernmentPaymentRepository;
import id.co.taxpay.taxgateway.infrastructure.persistence.SpringDataGovernmentPaymentResponseRepository;
import id.co.taxpay.taxgateway.interfaces.rest.GatewayException;
import id.co.taxpay.taxgateway.interfaces.rest.dto.SubmitPaymentRequest;
import id.co.taxpay.taxgateway.interfaces.rest.dto.SubmitPaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxGatewaySubmissionService {

    private final SpringDataGovernmentBillingRepository billingRepository;
    private final SpringDataGovernmentPaymentRepository paymentRepository;
    private final SpringDataGovernmentPaymentResponseRepository responseRepository;
    private final Clock clock;

    @Transactional
    public SubmitPaymentResponse submit(SubmitPaymentRequest request, String scenario) {
        var existing = paymentRepository.findByReference(request.submissionReference());
        if (existing.isPresent()) {
            GovernmentPaymentResponseEntity previous = responseRepository.findAll().stream()
                    .filter(r -> r.getPaymentReference().equals(request.submissionReference()))
                    .findFirst().orElse(null);
            String code = previous == null ? "MOCK-000" : previous.getResponseCode();
            String message = previous == null ? "MOCK duplicate submission ignored" : previous.getResponseMessage();
            return SubmitPaymentResponse.from(existing.get(), code, message, true);
        }

        GovernmentBillingRecordEntity billing = billingRepository.findByBillingId(request.billingId())
                .orElseThrow(() -> new GatewayException(ErrorCode.BILLING_NOT_FOUND,
                        "Government billing " + request.billingId() + " was not found"));
        if (!"OPEN".equals(billing.getStatus())) {
            throw new GatewayException(ErrorCode.BILLING_ALREADY_PAID,
                    "Government billing is not open for payment");
        }
        if (billing.getAmount().compareTo(request.amount().setScale(2, RoundingMode.UNNECESSARY)) != 0) {
            throw new GatewayException(ErrorCode.AMOUNT_MISMATCH,
                    "Government billing amount does not match submitted amount");
        }

        boolean rejected = "REJECTED".equalsIgnoreCase(scenario);
        Instant now = clock.instant();
        GovernmentTaxPaymentEntity payment = new GovernmentTaxPaymentEntity();
        payment.setId(UUID.randomUUID());
        payment.setReference(request.submissionReference());
        payment.setBillingId(request.billingId());
        payment.setAmount(request.amount().setScale(2, RoundingMode.UNNECESSARY));
        payment.setCurrency(request.currency().toUpperCase(Locale.ROOT));
        payment.setStatus(rejected ? "REJECTED" : "SUCCESS");
        payment.setMockNtb(rejected ? null : mockReference("NTB", 12));
        payment.setMockNtpn(rejected ? null : mockReference("NTPN", 16));
        payment.setSubmittedAt(now);
        payment.setRespondedAt(now);
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        payment.setVersion(0L);
        GovernmentTaxPaymentEntity saved = paymentRepository.saveAndFlush(payment);

        GovernmentPaymentResponseEntity response = new GovernmentPaymentResponseEntity();
        response.setId(UUID.randomUUID());
        response.setPaymentReference(saved.getReference());
        response.setResponseCode(rejected ? "MOCK-REJ-001" : "MOCK-000");
        response.setResponseMessage(rejected ? "MOCK: government payment rejected" : "MOCK: payment accepted");
        response.setReceivedAt(now);
        responseRepository.saveAndFlush(response);

        return SubmitPaymentResponse.from(saved, response.getResponseCode(), response.getResponseMessage(), false);
    }

    private String mockReference(String prefix, int length) {
        String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
        return "MOCK-" + prefix + "-" + raw.substring(0, Math.min(length, raw.length()));
    }
}
