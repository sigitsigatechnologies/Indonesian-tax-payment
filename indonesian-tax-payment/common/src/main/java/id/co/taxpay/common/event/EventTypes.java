package id.co.taxpay.common.event;

public final class EventTypes {

    public static final String TAX_PAYMENT_INITIATED = "TAX_PAYMENT_INITIATED";
    public static final String TAX_PAYMENT_VALIDATED = "TAX_PAYMENT_VALIDATED";
    public static final String TAX_PAYMENT_APPROVED = "TAX_PAYMENT_APPROVED";
    public static final String TAX_PAYMENT_DEBIT_REQUESTED = "TAX_PAYMENT_DEBIT_REQUESTED";
    public static final String TAX_PAYMENT_DEBIT_COMPLETED = "TAX_PAYMENT_DEBIT_COMPLETED";
    public static final String TAX_PAYMENT_DEBIT_FAILED = "TAX_PAYMENT_DEBIT_FAILED";
    public static final String TAX_PAYMENT_GOVERNMENT_SUBMITTED = "TAX_PAYMENT_GOVERNMENT_SUBMITTED";
    public static final String TAX_PAYMENT_COMPLETED = "TAX_PAYMENT_COMPLETED";
    public static final String TAX_PAYMENT_FAILED = "TAX_PAYMENT_FAILED";
    public static final String TAX_PAYMENT_REVERSAL_REQUESTED = "TAX_PAYMENT_REVERSAL_REQUESTED";
    public static final String TAX_PAYMENT_REVERSED = "TAX_PAYMENT_REVERSED";
    public static final String TAX_PAYMENT_RECONCILIATION_COMPLETED = "TAX_PAYMENT_RECONCILIATION_COMPLETED";
    public static final String TAX_PAYMENT_NOTIFICATION_REQUESTED = "TAX_PAYMENT_NOTIFICATION_REQUESTED";

    public static final int VERSION_V1 = 1;

    private EventTypes() {
    }
}
