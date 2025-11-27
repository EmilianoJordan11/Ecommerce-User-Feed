package com.emijor.user_feed.dto;

import com.emijor.user_feed.entities.ArticleRating;

import java.time.LocalDateTime;

public record ArticleRatingDTO(
        String articleId,
        Double averageRating,
        Integer totalReviews,
        LocalDateTime updatedAt
) {
    public ArticleRatingDTO(ArticleRating rating) {
        this(
            rating.getArticleId(),
            rating.getAverageRating(),
            rating.getTotalReviews(),
            rating.getUpdatedAt()
        );
    }
}
