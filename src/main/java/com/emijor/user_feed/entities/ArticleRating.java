package com.emijor.user_feed.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "article_rating")
public class ArticleRating {
    
    @Id
    @Column(name = "article_id")
    private String articleId;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating;

    @Column(name = "total_reviews", nullable = false)
    private Integer totalReviews;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ArticleRating(String articleId, Double averageRating, Integer totalReviews) {
        this.articleId = articleId;
        this.averageRating = averageRating;
        this.totalReviews = totalReviews;
        this.updatedAt = LocalDateTime.now();
    }

    public void recalculate(Double newAverage, Integer newTotal) {
        this.averageRating = newAverage;
        this.totalReviews = newTotal;
        this.updatedAt = LocalDateTime.now();
    }
}
