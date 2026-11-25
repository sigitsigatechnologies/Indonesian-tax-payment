package id.co.taxpay.taxgateway.interfaces.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SubmitPaymentRequest(
        @NotBlank @Size(max = 64) String submissionReference,
        @NotBlank @Size(max = 64) String billingId,
        @NotNull @DecimalMin(value = "0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
        @Size(max = 64) String taxpayerId,
        @Size(max = 64) String customerId
) {
}
