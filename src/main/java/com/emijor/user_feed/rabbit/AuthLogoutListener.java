package com.emijor.user_feed.rabbit;

import com.emijor.user_feed.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class AuthLogoutListener {

    private static final Logger logger = Logger.getLogger(AuthLogoutListener.class.getName());

    @Autowired
    private TokenService tokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = RabbitConfig.AUTH_QUEUE)
    public void handleMessage(byte[] messageBytes) {
        String message = new String(messageBytes, StandardCharsets.UTF_8);
        logger.log(Level.INFO, ">>> Mensaje RAW recibido de Auth (logout): " + message);
        
        try {
            // Parsear el JSON del mensaje de Auth
            RabbitEvent event = objectMapper.readValue(message, RabbitEvent.class);
            
            // Auth no envía "type" - todo mensaje del exchange "auth" es logout
            String token = event.getMessage() != null ? event.getMessage().toString() : null;
            
            if (token != null && !token.isBlank()) {
                logger.log(Level.INFO, "Evento logout recibido - correlation_id: " + event.getCorrelationId());
                processLogout(token);
            } else {
                logger.log(Level.WARNING, "Mensaje de Auth sin token en 'message'");
            }

        } catch (Exception e) {
            // Si no es JSON válido, tratar como token directo
            logger.log(Level.INFO, "No es JSON, tratando como token directo: " + e.getMessage());
            processLogout(message);
        }
    }

    private void processLogout(String token) {
        if (token == null || token.isBlank()) {
            logger.log(Level.WARNING, "Evento logout sin token");
            return;
        }

        logger.log(Level.INFO, "Procesando logout para token: " + token.substring(0, Math.min(token.length(), 30)) + "...");
        
        // Invalidar el token en el cache local
        tokenService.invalidateToken(token);
        
        logger.log(Level.INFO, "Token invalidado exitosamente del cache");
    }
}
