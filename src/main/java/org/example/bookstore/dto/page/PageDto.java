package org.example.bookstore.dto.page;

import java.util.List;

public record PageDto<T>(
        List<T> content,
        Integer pageNumber,
        Integer pageSize,
        Long totalElements,
        Integer totalPages,
        Boolean first,
        Boolean last
) {
}
