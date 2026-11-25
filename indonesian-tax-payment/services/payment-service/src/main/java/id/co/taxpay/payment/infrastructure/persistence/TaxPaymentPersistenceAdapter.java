package id.co.taxpay.payment.infrastructure.persistence;

import id.co.taxpay.payment.domain.model.TaxPayment;
import id.co.taxpay.payment.domain.port.TaxPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TaxPaymentPersistenceAdapter implements TaxPaymentRepository {

    private final SpringDataTaxPaymentRepository repository;
    private final PaymentEntityMapper mapper;

    @Override
    public TaxPayment save(TaxPayment payment) {
        TaxPaymentEntity saved = repository.saveAndFlush(mapper.toEntity(payment));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<TaxPayment> findById(UUID paymentId) {
        return repository.findById(paymentId).map(mapper::toDomain);
    }

    @Override
    public Optional<TaxPayment> findByReference(String reference) {
        return repository.findByPaymentReference(reference).map(mapper::toDomain);
    }

    @Override
    public List<TaxPayment> findByCustomerId(String customerId, int offset, int limit) {
        int page = limit <= 0 ? 0 : offset / limit;
        Page<TaxPaymentEntity> result = repository.findByCustomerIdOrderByCreatedAtDesc(
                customerId, PageRequest.of(page, limit));
        return result.getContent().stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countByCustomerId(String customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId, PageRequest.of(0, 1)).getTotalElements();
    }
}
