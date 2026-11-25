package id.co.taxpay.billing.application.model;

public record BillingValidationResult(
        boolean valid,
        String resultCode,
        String message,
        BillingView billing
) {
    public static BillingValidationResult valid(BillingView billing) {
        return new BillingValidationResult(true, "VALID", "Billing is valid for payment", billing);
    }

    public static BillingValidationResult invalid(String code, String message, BillingView billing) {
        return new BillingValidationResult(false, code, message, billing);
    }
}
