package id.co.taxpay.payment.domain.port;

import java.util.Map;

public interface AuditLogRepository {

    void append(String aggregateType,
                String aggregateId,
                String action,
                String actor,
                String correlationId,
                Map<String, Object> beforeState,
                Map<String, Object> afterState);
}
