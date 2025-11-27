package com.emijor.user_feed.services;

import com.emijor.user_feed.dto.CreateFeedDTO;
import com.emijor.user_feed.dto.FeedDTO;
import com.emijor.user_feed.dto.UpdateFeedDTO;
import com.emijor.user_feed.entities.Feed;
import com.emijor.user_feed.repository.FeedRepository;
import com.emijor.user_feed.security.User;
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

    /**
     * Obtiene un feed por su ID.
     */
    public FeedDTO getAFeed(Long id) throws NotFoundError {
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("Feed no encontrado"));
        return new FeedDTO(feed);
    }

    /**
     * Crea un nuevo feed.
     * El userId se obtiene del token de autenticación.
     */
    public FeedDTO createFeed(CreateFeedDTO dto, String userId) {
        Feed feed = new Feed(dto, userId);
        feed = feedRepository.save(feed);
        return new FeedDTO(feed);
    }

    /**
     * Actualiza un feed existente.
     * Solo el dueño del feed puede actualizarlo.
     */
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

        return new FeedDTO(feed);
    }

    /**
     * Elimina un feed.
     * Solo el dueño del feed o un administrador puede eliminarlo.
     */
    public void deleteFeed(Long id, User user) throws NotFoundError, ForbiddenError {
        Feed feed = feedRepository.findById(id)
                .orElseThrow(() -> new NotFoundError("Feed no encontrado"));

        // Validar que el usuario sea el dueño o admin
        if (!feed.getUserId().equals(user.id) && !user.isAdmin()) {
            throw new ForbiddenError("No tienes permiso para eliminar este feed");
        }

        feedRepository.deleteById(id);
    }

    /**
     * Obtiene todos los feeds de un artículo.
     */
    public List<FeedDTO> getFeedsByArticulo(String idArticle) {
        List<Feed> feeds = feedRepository.findByArticleId(idArticle);
        List<FeedDTO> feedDTOS = new ArrayList<>();
        for (Feed f : feeds) {
            feedDTOS.add(new FeedDTO(f));
        }
        return feedDTOS;
    }

    /**
     * Obtiene todos los feeds de un usuario específico.
     */
    public List<FeedDTO> getFeedsByUser(String userId) {
        List<Feed> feeds = feedRepository.findByUserId(userId);
        List<FeedDTO> feedDTOS = new ArrayList<>();
        for (Feed f : feeds) {
            feedDTOS.add(new FeedDTO(f));
        }
        return feedDTOS;
    }
}
