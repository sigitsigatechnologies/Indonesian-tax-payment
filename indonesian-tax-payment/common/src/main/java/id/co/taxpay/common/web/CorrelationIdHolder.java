package id.co.taxpay.common.web;

import java.util.Optional;

public final class CorrelationIdHolder {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    public static void set(String correlationId) {
        CURRENT.set(correlationId);
    }

    public static Optional<String> get() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static String getOrGenerate() {
        String value = CURRENT.get();
        return value == null ? CorrelationId.generate() : value;
    }

    public static void clear() {
        CURRENT.remove();
    }

    private CorrelationIdHolder() {
    }
}
