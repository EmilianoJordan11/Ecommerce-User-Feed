package com.emijor.user_feed.entities;

import com.emijor.user_feed.dto.CreateFeedDTO;
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
@Table(name = "feed")
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "article_id", nullable = false)
    private String articleId;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Feed(CreateFeedDTO feedDTO, String userId) {
        this.userId = userId;
        this.articleId = feedDTO.articleId();
        this.comment = feedDTO.comment();
        this.rating = feedDTO.rating();
        this.createdAt = LocalDateTime.now();
    }
}
