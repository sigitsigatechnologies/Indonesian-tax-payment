package id.co.taxpay.payment.interfaces.rest.dto;

import id.co.taxpay.payment.application.model.PaymentView;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        String reference,
        String billingId,
        String customerId,
        String debitAccountNumber,
        BigDecimal amount,
        String currency,
        String status,
        long version,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(PaymentView view) {
        return new PaymentResponse(
                view.paymentId(),
                view.reference(),
                view.billingId(),
                view.customerId(),
                maskAccount(view.debitAccountNumber()),
                view.amount(),
                view.currency(),
                view.status().name(),
                view.version(),
                view.createdAt(),
                view.updatedAt()
        );
    }

    private static String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return "****";
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }
}
