package com.emijor.user_feed.rabbit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RabbitEvent {
    
    @JsonProperty("type")
    private String type;

    @JsonProperty("correlation_id")
    private String correlationId;

    @JsonProperty("routing_key")
    private String routingKey;

    @JsonProperty("version")
    private int version;

    @JsonProperty("queue")
    private String queue;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("message")
    private Object message;

    public RabbitEvent() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RabbitEvent{" +
                "type='" + type + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", routingKey='" + routingKey + '\'' +
                ", version=" + version +
                ", queue='" + queue + '\'' +
                ", exchange='" + exchange + '\'' +
                ", message=" + message +
                '}';
    }
}
