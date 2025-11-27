package com.emijor.user_feed.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ArticleDTO(
        String articleId,
        int quantity,
        boolean isValid,
        float unitaryPrice,
        boolean isValidated
) {
}
