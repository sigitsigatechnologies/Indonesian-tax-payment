package id.co.taxpay.payment.application.command;

import java.math.BigDecimal;

public record InitiatePaymentCommand(
        String billingId,
        String debitAccountNumber,
        BigDecimal amount,
        String currency,
        String customerId
) {
}
