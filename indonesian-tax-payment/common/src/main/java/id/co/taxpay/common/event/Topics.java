package id.co.taxpay.common.event;

import java.util.List;

public final class Topics {

    public static final String PAYMENT_INITIATED = "tax.payment.initiated";
    public static final String PAYMENT_VALIDATED = "tax.payment.validated";
    public static final String PAYMENT_APPROVED = "tax.payment.approved";
    public static final String DEBIT_REQUESTED = "tax.payment.debit.requested";
    public static final String DEBIT_COMPLETED = "tax.payment.debit.completed";
    public static final String DEBIT_FAILED = "tax.payment.debit.failed";
    public static final String GOVERNMENT_SUBMITTED = "tax.payment.government.submitted";
    public static final String PAYMENT_COMPLETED = "tax.payment.completed";
    public static final String PAYMENT_FAILED = "tax.payment.failed";
    public static final String REVERSAL_REQUESTED = "tax.payment.reversal.requested";
    public static final String PAYMENT_REVERSED = "tax.payment.reversed";
    public static final String RECONCILIATION_COMPLETED = "tax.payment.reconciliation.completed";
    public static final String NOTIFICATION_REQUESTED = "tax.payment.notification.requested";
    public static final String DEAD_LETTER = "tax.payment.dlt";

    public static final List<String> ALL_TOPICS = List.of(
            PAYMENT_INITIATED,
            PAYMENT_VALIDATED,
            PAYMENT_APPROVED,
            DEBIT_REQUESTED,
            DEBIT_COMPLETED,
            DEBIT_FAILED,
            GOVERNMENT_SUBMITTED,
            PAYMENT_COMPLETED,
            PAYMENT_FAILED,
            REVERSAL_REQUESTED,
            PAYMENT_REVERSED,
            RECONCILIATION_COMPLETED,
            NOTIFICATION_REQUESTED,
            DEAD_LETTER
    );

    private Topics() {
    }
}
