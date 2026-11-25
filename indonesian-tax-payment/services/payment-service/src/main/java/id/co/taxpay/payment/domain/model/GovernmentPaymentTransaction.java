package id.co.taxpay.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record GovernmentPaymentTransaction(
        UUID id,
        UUID paymentId,
        String submissionReference,
        String billingId,
        BigDecimal amount,
        String currency,
        String status,
        String mockNtb,
        String mockNtpn,
        Map<String, Object> requestPayload,
        Map<String, Object> responsePayload,
        Instant submittedAt,
        Instant respondedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
