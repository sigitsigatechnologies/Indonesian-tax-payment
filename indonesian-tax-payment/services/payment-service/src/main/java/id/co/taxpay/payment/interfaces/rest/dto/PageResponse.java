package id.co.taxpay.payment.interfaces.rest.dto;

import id.co.taxpay.payment.application.model.PageView;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <S, T> PageResponse<T> from(PageView<S> view, Function<S, T> mapper) {
        return new PageResponse<>(
                view.content().stream().map(mapper).toList(),
                view.page(),
                view.size(),
                view.totalElements(),
                view.totalPages()
        );
    }
}
