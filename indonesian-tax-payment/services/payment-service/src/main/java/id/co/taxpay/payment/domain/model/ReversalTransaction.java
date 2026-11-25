package id.co.taxpay.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReversalTransaction(
        UUID id,
        UUID paymentId,
        UUID debitTransactionId,
        String reversalReference,
        BigDecimal amount,
        String currency,
        String status,
        String reason,
        Instant requestedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
