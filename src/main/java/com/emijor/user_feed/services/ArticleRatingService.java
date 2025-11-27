package com.emijor.user_feed.services;

import com.emijor.user_feed.dto.ArticleRatingDTO;
import com.emijor.user_feed.entities.ArticleRating;
import com.emijor.user_feed.entities.Feed;
import com.emijor.user_feed.repository.ArticleRatingRepository;
import com.emijor.user_feed.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArticleRatingService {

    @Autowired
    private ArticleRatingRepository articleRatingRepository;

    @Autowired
    private FeedRepository feedRepository;

    public Optional<ArticleRatingDTO> getArticleRating(String articleId) {
        return articleRatingRepository.findById(articleId)
                .map(ArticleRatingDTO::new);
    }

    @Transactional
    public void recalculateRating(String articleId) {
        List<Feed> feeds = feedRepository.findByArticleId(articleId);
        
        if (feeds.isEmpty()) {
            articleRatingRepository.deleteById(articleId);
            return;
        }

        double sum = 0;
        for (Feed feed : feeds) {
            sum += feed.getRating();
        }
        
        double average = sum / feeds.size();
        average = Math.round(average * 100.0) / 100.0;

        Optional<ArticleRating> existing = articleRatingRepository.findById(articleId);
        
        if (existing.isPresent()) {
            existing.get().recalculate(average, feeds.size());
        } else {
            ArticleRating newRating = new ArticleRating(articleId, average, feeds.size());
            articleRatingRepository.save(newRating);
        }
    }
}
