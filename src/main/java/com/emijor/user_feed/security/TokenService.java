package com.emijor.user_feed.security;

import com.emijor.user_feed.utils.cache.ExpiringMap;
import com.emijor.user_feed.utils.errors.ForbiddenError;
import com.emijor.user_feed.utils.errors.UnauthorizedError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio de autenticación y autorización.
 * 
 * Se encarga de:
 * - Validar tokens JWT contra el microservicio Auth
 * - Cachear usuarios autenticados para evitar consultas repetitivas
 * - Invalidar tokens cuando se recibe un logout desde RabbitMQ
 * 
 * El token debe pasarse en el header "Authorization": "bearer {token}"
 */
@Service
public class TokenService {
    
    private static final Logger logger = Logger.getLogger(TokenService.class.getName());
    
    // Cache de tokens con expiración: 1 hora de vida, verificación cada 5 minutos
    private final ExpiringMap<String, User> tokenCache = new ExpiringMap<>(60 * 60, 60 * 5);
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${security.auth-server-url}")
    private String authServerUrl;

    /**
     * Valida que el token sea válido y que el usuario tenga permisos de usuario.
     * 
     * @param authHeader Header de autorización ("bearer {token}")
     * @throws UnauthorizedError Si el token es inválido o el usuario no está autenticado
     */
    public void validateUser(String authHeader) throws UnauthorizedError {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedError();
        }
        
        User user = getUserFromToken(authHeader);
        if (user == null) {
            throw new UnauthorizedError();
        }
    }

    /**
     * Valida que el token sea válido y que el usuario tenga permisos de administrador.
     * 
     * @param authHeader Header de autorización ("bearer {token}")
     * @throws UnauthorizedError Si el token es inválido
     * @throws ForbiddenError Si el usuario no tiene permisos de admin
     */
    public void validateAdmin(String authHeader) throws UnauthorizedError, ForbiddenError {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedError();
        }
        
        User user = getUserFromToken(authHeader);
        if (user == null) {
            throw new UnauthorizedError();
        }
        
        if (!user.isAdmin()) {
            throw new ForbiddenError("Se requieren permisos de administrador");
        }
    }

    /**
     * Obtiene el usuario autenticado a partir del token.
     * Primero busca en cache, si no existe consulta al servicio Auth.
     * 
     * @param authHeader Header de autorización ("bearer {token}")
     * @return Usuario autenticado o null si el token es inválido
     */
    public User getUser(String authHeader) throws UnauthorizedError {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedError();
        }
        
        User user = getUserFromToken(authHeader);
        if (user == null) {
            throw new UnauthorizedError();
        }
        
        return user;
    }

    /**
     * Invalida un token en el cache local.
     * Este método es llamado cuando se recibe un mensaje de logout desde RabbitMQ.
     * 
     * @param token Token a invalidar
     */
    public void invalidateToken(String token) {
        tokenCache.remove(token);
        logger.log(Level.INFO, "Token invalidado del cache: " + token.substring(0, Math.min(token.length(), 20)) + "...");
    }

    /**
     * Obtiene el usuario desde el cache o consulta al servicio Auth.
     */
    private User getUserFromToken(String authHeader) {
        // Primero buscar en cache
        User cachedUser = tokenCache.get(authHeader);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        // Si no está en cache, consultar al servicio Auth
        User user = fetchUserFromAuthService(authHeader);
        if (user != null) {
            tokenCache.put(authHeader, user);
        }
        
        return user;
    }

    /**
     * Realiza una petición HTTP al servicio Auth para validar el token
     * y obtener los datos del usuario.
     */
    private User fetchUserFromAuthService(String authHeader) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(authServerUrl + "/users/current"))
                    .header("Authorization", authHeader)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.log(Level.WARNING, "Auth service returned status: " + response.statusCode());
                return null;
            }

            String body = response.body();
            if (body == null || body.isBlank()) {
                return null;
            }

            return objectMapper.readValue(body, User.class);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al consultar servicio Auth: " + e.getMessage(), e);
            return null;
        }
    }
}
