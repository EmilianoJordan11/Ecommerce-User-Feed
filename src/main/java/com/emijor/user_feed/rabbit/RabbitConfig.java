package com.emijor.user_feed.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuración de RabbitMQ para el microservicio User Feed.
 * 
 * Configura la conexión al exchange "auth" de tipo fanout para escuchar
 * los eventos de logout y poder invalidar los tokens en cache.
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    private static final Logger logger = Logger.getLogger(RabbitConfig.class.getName());

    /**
     * Nombre del exchange de Auth (fanout para broadcast)
     */
    public static final String AUTH_EXCHANGE = "auth";

    /**
     * Nombre de la cola para este microservicio
     * Cada microservicio tiene su propia cola para recibir los broadcasts
     */
    public static final String AUTH_QUEUE = "user-feed-auth-queue";

    /**
     * RabbitAdmin se encarga de declarar automáticamente exchanges, queues y bindings
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        logger.log(Level.INFO, "RabbitAdmin configurado para autodeclaración de exchange/queue");
        return admin;
    }

    /**
     * Declara el exchange de tipo fanout para Auth.
     * Fanout = broadcast a todas las colas conectadas.
     * durable: false - debe coincidir con la configuración de Auth
     */
    @Bean
    public FanoutExchange authExchange() {
        logger.log(Level.INFO, "Declarando exchange fanout: " + AUTH_EXCHANGE);
        return new FanoutExchange(AUTH_EXCHANGE, false, false);
    }

    /**
     * Declara la cola para este microservicio.
     * - durable: false (no necesita persistir, es solo cache)
     * - exclusive: false (puede ser accedida por otras conexiones)
     * - autoDelete: true (se elimina cuando no hay consumidores)
     */
    @Bean
    public Queue authQueue() {
        logger.log(Level.INFO, "Declarando queue: " + AUTH_QUEUE);
        return new Queue(AUTH_QUEUE, false, false, true);
    }

    /**
     * Vincula la cola al exchange fanout.
     * No necesita routing key porque es fanout.
     */
    @Bean
    public Binding authBinding(Queue authQueue, FanoutExchange authExchange) {
        logger.log(Level.INFO, "Vinculando queue " + AUTH_QUEUE + " al exchange " + AUTH_EXCHANGE);
        return BindingBuilder.bind(authQueue).to(authExchange);
    }
}
