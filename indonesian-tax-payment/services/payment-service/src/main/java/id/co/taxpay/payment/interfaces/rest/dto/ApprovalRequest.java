package id.co.taxpay.payment.interfaces.rest.dto;

import jakarta.validation.constraints.Size;

public record ApprovalRequest(
        @Size(max = 512) String notes
) {
}
