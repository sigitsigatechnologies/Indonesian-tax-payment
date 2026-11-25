package id.co.taxpay.taxgateway.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "government_payment_responses")
@Getter
@Setter
@NoArgsConstructor
public class GovernmentPaymentResponseEntity {

    @Id
    private UUID id;

    @Column(name = "payment_reference", nullable = false, length = 64)
    private String paymentReference;

    @Column(name = "response_code", nullable = false, length = 32)
    private String responseCode;

    @Column(name = "response_message", length = 512)
    private String responseMessage;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
}
