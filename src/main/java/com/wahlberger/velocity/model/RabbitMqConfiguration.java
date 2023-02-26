package com.wahlberger.velocity.model;

public class RabbitMqConfiguration {
    private String host;
    private int port;
    private String virtualHost;
    private String username;
    private String password;
    private String publishExchangeName;
    private String routingKey;
    private String consumeQueueName;
    
    public RabbitMqConfiguration(String host, int port, String virtualHost, String username, String password,
            String publishExchangeName, String routingKey, String consumeQueueName) {
        this.host = host;
        this.port = port;
        this.virtualHost = virtualHost;
        this.username = username;
        this.password = password;
        this.publishExchangeName = publishExchangeName;
        this.routingKey = routingKey;
        this.consumeQueueName = consumeQueueName;
    }
    
    public String getConsumeQueueName() {
        return consumeQueueName;
    }

    public void setConsumeQueueName(String consumeQueueName) {
        this.consumeQueueName = consumeQueueName;
    }

    public String getRoutingKey() {
        return routingKey;
    }
    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
    public String getHost() {
        return host;
    }
    public String getPublishExchangeName() {
        return publishExchangeName;
    }
    public void setPublishExchangeName(String publishExchangeName) {
        this.publishExchangeName = publishExchangeName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getVirtualHost() {
        return virtualHost;
    }
    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public void setHost(String host) {
        this.host = host;
    }
}
