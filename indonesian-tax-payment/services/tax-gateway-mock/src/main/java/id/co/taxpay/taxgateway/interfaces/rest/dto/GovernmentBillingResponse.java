package id.co.taxpay.taxgateway.interfaces.rest.dto;

import id.co.taxpay.taxgateway.infrastructure.persistence.GovernmentBillingRecordEntity;

import java.math.BigDecimal;

public record GovernmentBillingResponse(
        String billingId,
        String taxpayerId,
        BigDecimal amount,
        String currency,
        String status
) {
    public static GovernmentBillingResponse from(GovernmentBillingRecordEntity entity) {
        return new GovernmentBillingResponse(entity.getBillingId(), entity.getTaxpayerId(),
                entity.getAmount(), entity.getCurrency(), entity.getStatus());
    }
}
