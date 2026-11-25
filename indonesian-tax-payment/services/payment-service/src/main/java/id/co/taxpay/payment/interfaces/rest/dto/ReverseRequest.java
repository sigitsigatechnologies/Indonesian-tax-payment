package id.co.taxpay.payment.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReverseRequest(
        @NotBlank @Size(max = 255) String reason
) {
}
