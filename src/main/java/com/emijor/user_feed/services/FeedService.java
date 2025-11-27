package com.emijor.user_feed.services;

import com.emijor.user_feed.dto.CreateFeedDTO;
import com.emijor.user_feed.dto.FeedDTO;
import com.emijor.user_feed.dto.UpdateFeedDTO;
import com.emijor.user_feed.entities.Feed;
import com.emijor.user_feed.repository.FeedRepository;
import com.emijor.user_feed.security.User;
import com.emijor.user_feed.utils.errors.BadRequestError;
import com.emijor.user_feed.utils.errors.ForbiddenError;
import com.emijor.user_feed.utils.errors.NotFoundError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ArticleRatingService articleRatingService;

    public FeedDTO getAFeed(Long id) throws NotFoundError {
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("Feed no encontrado"));
        return new FeedDTO(feed);
    }

    public FeedDTO createFeed(CreateFeedDTO dto, String userId, String token) throws BadRequestError {
        // Validación 1: El usuario no puede tener más de una reseña por artículo
        List<Feed> existingReviews = feedRepository.findByUserIdAndArticleId(userId, dto.articleId());
        if (!existingReviews.isEmpty()) {
            throw new BadRequestError("Ya tienes una reseña para este artículo");
        }

        // Validación 2: El usuario debe tener alguna orden con ese artículo
        if (!orderService.userHasOrderWithArticle(token, dto.articleId())) {
            throw new BadRequestError(
                "No puedes reseñar este artículo. Solo puedes reseñar artículos que hayas comprado"
            );
        }

        // Crear el feed
        Feed feed = new Feed(dto, userId);
        feed = feedRepository.save(feed);
        
        // Recalcular promedio del artículo
        articleRatingService.recalculateRating(dto.articleId());
        
        return new FeedDTO(feed);
    }

    @Transactional
    public FeedDTO updateFeed(Long id, UpdateFeedDTO dto, String userId) throws NotFoundError, ForbiddenError {
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("Feed no encontrado"));

        // Validar que el usuario sea el dueño del feed
        if (!feed.getUserId().equals(userId)) {
            throw new ForbiddenError("No tienes permiso para modificar este feed");
        }

        if (dto.comment() != null) feed.setComment(dto.comment());
        if (dto.rating() != null) feed.setRating(dto.rating());

        if (dto.comment() != null || dto.rating() != null) {
            feed.setUpdatedAt(LocalDateTime.now());
        }

        // Recalcular promedio si cambió el rating
        if (dto.rating() != null) {
            articleRatingService.recalculateRating(feed.getArticleId());
        }

        return new FeedDTO(feed);
    }

    public void deleteFeed(Long id, User user) throws NotFoundError, ForbiddenError {
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("Feed no encontrado"));

        // Validar que el usuario sea el dueño o admin
        if (!feed.getUserId().equals(user.id) && !user.isAdmin()) {
            throw new ForbiddenError("No tienes permiso para eliminar este feed");
        }

        String articleId = feed.getArticleId();
        feedRepository.deleteById(id);
        
        // Recalcular promedio del artículo
        articleRatingService.recalculateRating(articleId);
    }

    public List<FeedDTO> getFeedsByArticulo(String idArticle) {
        List<Feed> feeds = feedRepository.findByArticleId(idArticle);
        List<FeedDTO> feedDTOS = new ArrayList<>();
        for (Feed f : feeds) {
            feedDTOS.add(new FeedDTO(f));
        }
        return feedDTOS;
    }

    public List<FeedDTO> getFeedsByUser(String userId) {
        List<Feed> feeds = feedRepository.findByUserId(userId);
        List<FeedDTO> feedDTOS = new ArrayList<>();
        for (Feed f : feeds) {
            feedDTOS.add(new FeedDTO(f));
        }
        return feedDTOS;
    }
}
