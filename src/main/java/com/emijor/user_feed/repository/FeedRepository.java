package com.emijor.user_feed.repository;

import com.emijor.user_feed.entities.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    List<Feed> findByArticleId(String articleId);

    List<Feed> findByUserId(String userId);

    List<Feed> findByUserIdAndArticleId(String userId, String articleId);
}
