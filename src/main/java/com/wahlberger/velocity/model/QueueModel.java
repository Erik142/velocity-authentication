package com.wahlberger.velocity.model;

public abstract class QueueModel {
    private String command;
    private String user;
    private String server;
    
    public QueueModel(String user, String command) {
        this.user = user;
        this.command = command;
    }

    public QueueModel(String user, String command, String server) {
        this.user = user;
        this.command = command;
        this.server = server;
    }

    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }
    public String getCommand() {
        return command;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
}
