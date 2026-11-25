package id.co.taxpay.payment.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataBankDebitRepository extends JpaRepository<BankDebitTransactionEntity, UUID> {

    Optional<BankDebitTransactionEntity> findByPaymentId(UUID paymentId);

    Optional<BankDebitTransactionEntity> findByDebitReference(String debitReference);
}
