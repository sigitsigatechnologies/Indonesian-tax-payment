package id.co.taxpay.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record BankDebitTransaction(
        UUID id,
        UUID paymentId,
        String debitReference,
        String accountNumber,
        BigDecimal amount,
        String currency,
        String status,
        String coreBankingReference,
        Map<String, Object> requestPayload,
        Map<String, Object> responsePayload,
        Instant debitAt,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
