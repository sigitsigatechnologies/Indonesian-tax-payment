package id.co.taxpay.common.web;

import java.util.UUID;

public final class CorrelationId {

    public static final String HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return generate();
        }
        String trimmed = value.trim();
        if (trimmed.length() > 128 || !trimmed.matches("[A-Za-z0-9._:-]+")) {
            return generate();
        }
        return trimmed;
    }

    private CorrelationId() {
    }
}
