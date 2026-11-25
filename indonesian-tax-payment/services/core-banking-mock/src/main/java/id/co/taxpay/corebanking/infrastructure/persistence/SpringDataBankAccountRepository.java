package id.co.taxpay.corebanking.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataBankAccountRepository extends JpaRepository<BankAccountEntity, UUID> {

    Optional<BankAccountEntity> findByAccountNumber(String accountNumber);
}
