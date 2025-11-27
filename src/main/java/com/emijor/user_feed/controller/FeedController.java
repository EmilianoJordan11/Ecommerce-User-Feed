package com.emijor.user_feed.controller;

import com.emijor.user_feed.dto.CreateFeedDTO;
import com.emijor.user_feed.dto.FeedDTO;
import com.emijor.user_feed.dto.UpdateFeedDTO;
import com.emijor.user_feed.security.TokenService;
import com.emijor.user_feed.security.User;
import com.emijor.user_feed.services.FeedService;
import com.emijor.user_feed.utils.errors.ForbiddenError;
import com.emijor.user_feed.utils.errors.NotFoundError;
import com.emijor.user_feed.utils.errors.UnauthorizedError;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el módulo User Feed.
 * Permite a los usuarios comentar sobre artículos que compraron.
 * 
 * Todos los endpoints requieren autenticación excepto la consulta de feeds por artículo.
 */
@RestController
@RequestMapping("/v1/feed")
@CrossOrigin
public class FeedController {

    @Autowired
    private FeedService feedService;

    @Autowired
    private TokenService tokenService;

    /**
     * Obtiene un feed por su ID.
     * No requiere autenticación (público).
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeedDTO> getAFeed(@PathVariable Long id) throws NotFoundError {
        FeedDTO feed = feedService.getAFeed(id);
        return ResponseEntity.ok(feed);
    }

    /**
     * Crea un nuevo feed/comentario.
     * Requiere autenticación. El userId se obtiene del token.
     */
    @PostMapping
    public ResponseEntity<FeedDTO> createFeed(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestBody @Valid CreateFeedDTO dto
    ) throws UnauthorizedError {
        // Validar que el usuario esté autenticado
        User user = tokenService.getUser(authHeader);
        
        // Crear el feed con el userId del token
        FeedDTO feed = feedService.createFeed(dto, user.id);
        return ResponseEntity.status(201).body(feed);
    }

    /**
     * Actualiza un feed existente.
     * Requiere autenticación. Solo el dueño del feed puede actualizarlo.
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeedDTO> updateFeed(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid UpdateFeedDTO dto
    ) throws UnauthorizedError, ForbiddenError, NotFoundError {
        User user = tokenService.getUser(authHeader);
        
        // El servicio validará que el usuario sea el dueño del feed
        FeedDTO feed = feedService.updateFeed(id, dto, user.id);
        return ResponseEntity.ok(feed);
    }

    /**
     * Elimina un feed.
     * Requiere autenticación. Solo el dueño del feed o un admin puede eliminarlo.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeed(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable Long id
    ) throws UnauthorizedError, ForbiddenError, NotFoundError {
        User user = tokenService.getUser(authHeader);
        
        // El servicio validará que el usuario sea el dueño o admin
        feedService.deleteFeed(id, user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene todos los feeds/comentarios de un artículo.
     * No requiere autenticación (público).
     */
    @GetMapping("/article/{idArticle}")
    public ResponseEntity<List<FeedDTO>> getFeedsByArticle(@PathVariable String idArticle) {
        List<FeedDTO> feedDTOS = feedService.getFeedsByArticulo(idArticle);
        return ResponseEntity.ok(feedDTOS);
    }

    /**
     * Obtiene todos los feeds del usuario autenticado.
     * Requiere autenticación.
     */
    @GetMapping("/my-feeds")
    public ResponseEntity<List<FeedDTO>> getMyFeeds(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader
    ) throws UnauthorizedError {
        User user = tokenService.getUser(authHeader);
        List<FeedDTO> feeds = feedService.getFeedsByUser(user.id);
        return ResponseEntity.ok(feeds);
    }
}
