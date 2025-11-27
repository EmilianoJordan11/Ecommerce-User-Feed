package com.emijor.user_feed.services;

import com.emijor.user_feed.dto.order.OrderDTO;
import com.emijor.user_feed.dto.order.OrderDetailDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class OrderService {

    private static final Logger logger = Logger.getLogger(OrderService.class.getName());

    @Value("${orders-server-url}")
    private String ordersServerUrl;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<OrderDTO> getUserOrders(String token) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ordersServerUrl + "/orders"))
                    .header("Authorization", token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<OrderDTO>>() {});
            } else {
                logger.log(Level.WARNING, "Orders service returned status: " + response.statusCode());
                return Collections.emptyList();
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al consultar órdenes: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<OrderDetailDTO> getOrderDetail(String token, String orderId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ordersServerUrl + "/orders/" + orderId))
                    .header("Authorization", token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OrderDetailDTO order = objectMapper.readValue(response.body(), OrderDetailDTO.class);
                return Optional.of(order);
            } else if (response.statusCode() == 404) {
                logger.log(Level.INFO, "Orden no encontrada: " + orderId);
                return Optional.empty();
            } else {
                logger.log(Level.WARNING, "Orders service returned status: " + response.statusCode());
                return Optional.empty();
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al consultar orden " + orderId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean orderContainsArticle(String token, String orderId, String articleId) {
        Optional<OrderDetailDTO> order = getOrderDetail(token, orderId);
        return order.isPresent() && order.get().hasArticle(articleId);
    }

    public boolean orderExists(String token, String orderId) {
        return getOrderDetail(token, orderId).isPresent();
    }

    public boolean userHasOrderWithArticle(String token, String articleId) {
        List<OrderDTO> orders = getUserOrders(token);
        
        for (OrderDTO order : orders) {
            // Obtener el detalle de cada orden para ver sus artículos
            Optional<OrderDetailDTO> detail = getOrderDetail(token, order.id());
            if (detail.isPresent() && detail.get().hasArticle(articleId)) {
                logger.log(Level.INFO, "Usuario tiene orden " + order.id() + " con artículo " + articleId);
                return true;
            }
        }
        
        logger.log(Level.INFO, "Usuario no tiene ninguna orden con el artículo " + articleId);
        return false;
    }
}
