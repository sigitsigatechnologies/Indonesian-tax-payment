package id.co.taxpay.billing.infrastructure.persistence;

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
@Table(name = "taxpayer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class TaxpayerProfileEntity {

    @Id
    private UUID id;

    @Column(name = "taxpayer_id", nullable = false, unique = true, length = 64)
    private String taxpayerId;

    @Column(name = "taxpayer_name", nullable = false, length = 255)
    private String taxpayerName;

    @Column(name = "npwp", nullable = false, unique = true, length = 32)
    private String npwp;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
