package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.GovernmentPaymentTransaction;
import id.co.taxpay.payment.domain.port.GovernmentPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GovernmentPaymentPersistenceAdapter implements GovernmentPaymentRepository {

    private final SpringDataGovernmentPaymentRepository repository;

    @Override
    public GovernmentPaymentTransaction save(GovernmentPaymentTransaction transaction) {
        GovernmentPaymentTransactionEntity entity = new GovernmentPaymentTransactionEntity();
        entity.setId(transaction.id());
        entity.setPaymentId(transaction.paymentId());
        entity.setSubmissionReference(transaction.submissionReference());
        entity.setBillingId(transaction.billingId());
        entity.setAmount(transaction.amount());
        entity.setCurrency(transaction.currency());
        entity.setStatus(transaction.status());
        entity.setMockNtb(transaction.mockNtb());
        entity.setMockNtpn(transaction.mockNtpn());
        entity.setRequestPayload(transaction.requestPayload());
        entity.setResponsePayload(transaction.responsePayload());
        entity.setSubmittedAt(transaction.submittedAt());
        entity.setRespondedAt(transaction.respondedAt());
        entity.setCreatedAt(transaction.createdAt());
        entity.setUpdatedAt(transaction.updatedAt());
        entity.setVersion(transaction.version());
        return toDomain(repository.saveAndFlush(entity));
    }

    @Override
    public Optional<GovernmentPaymentTransaction> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentId(paymentId).map(this::toDomain);
    }

    @Override
    public Optional<GovernmentPaymentTransaction> findByReference(String submissionReference) {
        return repository.findBySubmissionReference(submissionReference).map(this::toDomain);
    }

    private GovernmentPaymentTransaction toDomain(GovernmentPaymentTransactionEntity entity) {
        return new GovernmentPaymentTransaction(
                entity.getId(), entity.getPaymentId(), entity.getSubmissionReference(), entity.getBillingId(),
                entity.getAmount(), entity.getCurrency(), entity.getStatus(), entity.getMockNtb(), entity.getMockNtpn(),
                entity.getRequestPayload(), entity.getResponsePayload(), entity.getSubmittedAt(), entity.getRespondedAt(),
                entity.getCreatedAt(), entity.getUpdatedAt(), entity.getVersion()
        );
    }
}
