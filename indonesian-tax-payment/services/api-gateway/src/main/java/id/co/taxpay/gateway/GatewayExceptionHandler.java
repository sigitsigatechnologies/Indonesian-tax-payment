package id.co.taxpay.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.co.taxpay.common.error.ApiError;
import id.co.taxpay.common.error.ErrorCode;
import id.co.taxpay.common.util.JsonMapperFactory;
import id.co.taxpay.common.web.CorrelationId;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
@Order(-2)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = JsonMapperFactory.create();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        HttpStatus status = resolveStatus(ex);
        ErrorCode errorCode = resolveErrorCode(status);
        String correlationId = CorrelationId.normalize(
                exchange.getRequest().getHeaders().getFirst(CorrelationId.HEADER));
        ApiError error = new ApiError(
                Instant.now(),
                status.value(),
                errorCode.code(),
                errorCode.defaultMessage(),
                exchange.getRequest().getPath().value(),
                correlationId,
                List.of()
        );
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(CorrelationId.HEADER, correlationId);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(error);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (Exception serializationError) {
            return Mono.error(ex);
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            HttpStatus resolved = HttpStatus.resolve(responseStatusException.getStatusCode().value());
            return resolved == null ? HttpStatus.INTERNAL_SERVER_ERROR : resolved;
        }
        if (hasCause(ex, TimeoutException.class)) {
            return HttpStatus.GATEWAY_TIMEOUT;
        }
        if (hasCause(ex, ConnectException.class)) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private ErrorCode resolveErrorCode(HttpStatus status) {
        if (status == HttpStatus.GATEWAY_TIMEOUT) {
            return ErrorCode.UPSTREAM_TIMEOUT;
        }
        if (status == HttpStatus.SERVICE_UNAVAILABLE) {
            return ErrorCode.UPSTREAM_UNAVAILABLE;
        }
        if (status == HttpStatus.NOT_FOUND) {
            return ErrorCode.NOT_FOUND;
        }
        return ErrorCode.INTERNAL_ERROR;
    }

    private boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
        Throwable current = ex;
        while (current != null) {
            if (type.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
