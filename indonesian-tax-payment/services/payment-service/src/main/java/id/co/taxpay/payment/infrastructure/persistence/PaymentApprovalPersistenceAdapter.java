package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.PaymentApproval;
import id.co.taxpay.payment.domain.port.PaymentApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentApprovalPersistenceAdapter implements PaymentApprovalRepository {

    private final SpringDataTaxPaymentApprovalRepository repository;
    private final PaymentEntityMapper mapper;

    @Override
    public PaymentApproval save(PaymentApproval approval) {
        return mapper.toDomain(repository.save(mapper.toEntity(approval)));
    }

    @Override
    public Optional<PaymentApproval> findByPaymentId(UUID paymentId) {
        return repository.findByPaymentId(paymentId).map(mapper::toDomain);
    }
}
