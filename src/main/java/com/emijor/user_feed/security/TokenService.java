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

@Service
public class TokenService {
    
    private static final Logger logger = Logger.getLogger(TokenService.class.getName());
    
    private final ExpiringMap<String, User> tokenCache = new ExpiringMap<>(60 * 60, 60 * 5);
    
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${security.auth-server-url}")
    private String authServerUrl;

    public void validateUser(String authHeader) throws UnauthorizedError {
        if (authHeader == null || authHeader.isBlank()) {
            throw new UnauthorizedError();
        }
        
        User user = getUserFromToken(authHeader);
        if (user == null) {
            throw new UnauthorizedError();
        }
    }

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

    public void invalidateToken(String token) {
        tokenCache.remove(token);
        logger.log(Level.INFO, "Token invalidado del cache: " + token.substring(0, Math.min(token.length(), 20)) + "...");
    }

    private User getUserFromToken(String authHeader) {
        User cachedUser = tokenCache.get(authHeader);
        if (cachedUser != null) {
            return cachedUser;
        }
        
        User user = fetchUserFromAuthService(authHeader);
        if (user != null) {
            tokenCache.put(authHeader, user);
        }
        
        return user;
    }

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
