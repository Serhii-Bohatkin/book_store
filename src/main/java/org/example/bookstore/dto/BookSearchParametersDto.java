package org.example.bookstore.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record BookSearchParametersDto(
        String title,
        String author,
        @Pattern(regexp = ISBN_REGEXP, message = "must be 10 or 13 digit ISBN code")
        String isbn,
        @Positive
        Integer minPrice,
        @Positive
        Integer maxPrice
) {
    private static final String ISBN_REGEXP = "^(?:ISBN(?:-1[03])?:? )?(?=[-0-9 ]{17}$|[-0-9X ]"
            + "{13}$|[0-9X]{10}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?(?:[0-9]+[- ]?){2}[0-9X]$";
}
