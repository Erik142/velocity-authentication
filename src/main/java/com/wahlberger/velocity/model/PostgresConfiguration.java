package com.wahlberger.velocity.model;

public class PostgresConfiguration {
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public PostgresConfiguration(String host, int port, String database, String username, String password) {
        this.setHost(host);
        this.setPort(port);
        this.setDatabase(database);
        this.setUsername(username);
        this.setPassword(password);
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
