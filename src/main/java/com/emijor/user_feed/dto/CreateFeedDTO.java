package com.emijor.user_feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateFeedDTO (
        @NotBlank
        String userId,
        @NotBlank
        String articleId,
        @NotBlank
        String orderId,
        String comment,
        @NotNull
        Integer rating
){
}
