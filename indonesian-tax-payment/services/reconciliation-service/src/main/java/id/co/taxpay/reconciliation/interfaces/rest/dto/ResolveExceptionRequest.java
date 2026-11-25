package id.co.taxpay.reconciliation.interfaces.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResolveExceptionRequest(
        @NotBlank @Size(max = 255) String note
) {
}
