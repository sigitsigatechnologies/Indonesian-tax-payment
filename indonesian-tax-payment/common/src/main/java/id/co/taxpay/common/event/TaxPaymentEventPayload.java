package id.co.taxpay.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Shared payload for payment lifecycle events. Fields that do not apply to a
 * particular event are null rather than being fabricated.
 */
public record TaxPaymentEventPayload(
        UUID paymentId,
        String reference,
        String billingId,
        String customerId,
        BigDecimal amount,
        String currency,
        String status,
        String reason,
        String debitReference,
        String governmentReference,
        Instant occurredAt
) {
}
