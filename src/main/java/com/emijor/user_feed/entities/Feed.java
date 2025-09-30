package com.emijor.user_feed.entities;

import com.emijor.user_feed.dto.CreateFeedDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String userId;
    private String articleId;
    private String orderId;
    private String comment;
    private Integer rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Feed(CreateFeedDTO feedDTO) {
        this.userId = feedDTO.userId();
        this.articleId = feedDTO.articleId();
        this.orderId = feedDTO.orderId();
        this.comment = feedDTO.comment();
        this.rating = feedDTO.rating();
        this.createdAt = LocalDateTime.now();
    }
}
