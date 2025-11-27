package com.emijor.user_feed.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableRabbit
public class RabbitConfig {

    private static final Logger logger = Logger.getLogger(RabbitConfig.class.getName());

    public static final String AUTH_EXCHANGE = "auth";
    public static final String AUTH_QUEUE = "user-feed-auth-queue";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        logger.log(Level.INFO, "RabbitAdmin configurado para autodeclaración de exchange/queue");
        return admin;
    }

    @Bean
    public FanoutExchange authExchange() {
        logger.log(Level.INFO, "Declarando exchange fanout: " + AUTH_EXCHANGE);
        return new FanoutExchange(AUTH_EXCHANGE, false, false);
    }

    @Bean
    public Queue authQueue() {
        logger.log(Level.INFO, "Declarando queue: " + AUTH_QUEUE);
        return new Queue(AUTH_QUEUE, false, false, true);
    }

    @Bean
    public Binding authBinding(Queue authQueue, FanoutExchange authExchange) {
        logger.log(Level.INFO, "Vinculando queue " + AUTH_QUEUE + " al exchange " + AUTH_EXCHANGE);
        return BindingBuilder.bind(authQueue).to(authExchange);
    }
}
