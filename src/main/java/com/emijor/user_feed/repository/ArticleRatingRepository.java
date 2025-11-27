package com.emijor.user_feed.repository;

import com.emijor.user_feed.entities.ArticleRating;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRatingRepository extends JpaRepository<ArticleRating, String> {
}
