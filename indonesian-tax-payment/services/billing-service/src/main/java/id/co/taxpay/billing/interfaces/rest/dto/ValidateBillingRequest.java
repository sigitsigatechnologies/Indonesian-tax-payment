package id.co.taxpay.billing.interfaces.rest.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ValidateBillingRequest(
        @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @Pattern(regexp = "[A-Za-z]{3}") String currency,
        @Size(max = 64) String taxpayerId
) {
}
