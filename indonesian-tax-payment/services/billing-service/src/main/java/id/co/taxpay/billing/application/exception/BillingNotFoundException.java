package id.co.taxpay.billing.application.exception;

import id.co.taxpay.common.error.ErrorCode;

public class BillingNotFoundException extends BillingException {

    public BillingNotFoundException(String billingId) {
        super(ErrorCode.BILLING_NOT_FOUND, "Billing " + billingId + " was not found");
    }
}
