package com.emijor.user_feed.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateFeedDTO(
        @NotBlank(message = "El articleId es requerido")
        String articleId,

        String comment,

        @NotNull(message = "El rating es requerido")
        @Min(value = 1, message = "El rating mínimo es 1")
        @Max(value = 5, message = "El rating máximo es 5")
        Integer rating
) {
}
