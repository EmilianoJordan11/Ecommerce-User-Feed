package com.emijor.user_feed.dto;

import com.emijor.user_feed.entities.Feed;

import java.time.LocalDateTime;

public record FeedDTO(
        Long id,
        String userId,
        String articleId,
        String comment,
        Integer rating,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public FeedDTO(Feed feed) {
        this(
            feed.getId(),
            feed.getUserId(),
            feed.getArticleId(),
            feed.getComment(),
            feed.getRating(),
            feed.getCreatedAt(),
            feed.getUpdatedAt()
        );
    }
}
