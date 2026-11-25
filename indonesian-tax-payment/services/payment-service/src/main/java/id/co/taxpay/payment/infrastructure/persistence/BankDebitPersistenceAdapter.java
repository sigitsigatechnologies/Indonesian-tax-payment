package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.BankDebitTransaction;
import id.co.taxpay.payment.domain.port.BankDebitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BankDebitPersistenceAdapter implements BankDebitRepository {

    private final SpringDataBankDebitRepository repository;

    @Override
    public BankDebitTransaction save(BankDebitTransaction transaction) {
        BankDebitTransactionEntity entity = new BankDebitTransactionEntity();
        entity.setId(transaction.id());
        entity.setPaymentId(transaction.paymentId());
        entity.setDebitReference(transaction.debitReference());
        entity.setAccountNumber(transaction.accountNumber());
        entity.setAmount(transaction.amount());
        entity.setCurrency(transaction.currency());
        entity.setStatus(transaction.status());
        entity.setCoreBankingReference(transaction.coreBankingReference());
        entity.setRequestPayload(transaction.requestPayload());
        entity.setResponsePayload(transaction.responsePayload());
        entity.setDebitAt(transaction.debitAt());
        entity.setCreatedAt(transaction.createdAt());
        entity.setUpdatedAt(transaction.updatedAt());
        entity.setVersion(transaction.version());
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<BankDebitTransaction> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentId(paymentId).map(this::toDomain);
    }

    @Override
    public Optional<BankDebitTransaction> findByReference(String debitReference) {
        return repository.findByDebitReference(debitReference).map(this::toDomain);
    }

    private BankDebitTransaction toDomain(BankDebitTransactionEntity entity) {
        return new BankDebitTransaction(
                entity.getId(), entity.getPaymentId(), entity.getDebitReference(), entity.getAccountNumber(),
                entity.getAmount(), entity.getCurrency(), entity.getStatus(), entity.getCoreBankingReference(),
                entity.getRequestPayload(), entity.getResponsePayload(), entity.getDebitAt(),
                entity.getCreatedAt(), entity.getUpdatedAt(), entity.getVersion()
        );
    }
}
