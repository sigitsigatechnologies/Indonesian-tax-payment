package id.co.taxpay.payment.domain.port;

import id.co.taxpay.payment.domain.model.BankDebitTransaction;

import java.util.Optional;
import java.util.UUID;

public interface BankDebitRepository {

    BankDebitTransaction save(BankDebitTransaction transaction);

    Optional<BankDebitTransaction> findByPaymentId(UUID paymentId);

    Optional<BankDebitTransaction> findByReference(String debitReference);
}
