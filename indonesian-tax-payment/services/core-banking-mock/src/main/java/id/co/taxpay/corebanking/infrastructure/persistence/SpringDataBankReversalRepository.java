package id.co.taxpay.corebanking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataBankReversalRepository extends JpaRepository<BankReversalTransactionEntity, UUID> {

    Optional<BankReversalTransactionEntity> findByReversalReference(String reversalReference);
}
