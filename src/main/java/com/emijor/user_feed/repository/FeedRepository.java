package com.emijor.user_feed.repository;

import com.emijor.user_feed.entities.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    /**
     * Busca todos los feeds de un artículo específico.
     */
    List<Feed> findByArticleId(String articleId);

    /**
     * Busca todos los feeds de un usuario específico.
     */
    List<Feed> findByUserId(String userId);

    /**
     * Busca todos los feeds de un usuario para un artículo específico.
     */
    List<Feed> findByUserIdAndArticleId(String userId, String articleId);
}
