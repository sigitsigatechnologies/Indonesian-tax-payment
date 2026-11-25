package id.co.taxpay.corebanking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataBankTransactionRepository extends JpaRepository<BankTransactionEntity, UUID> {

    Optional<BankTransactionEntity> findByTransactionReference(String transactionReference);
}
