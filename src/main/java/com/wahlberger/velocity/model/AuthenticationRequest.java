package com.wahlberger.velocity.model;

public class AuthenticationRequest {
    private int id;
    private String minecraftUsername;
    private String minecraftServer;
    private boolean isHandled;

    public AuthenticationRequest(int id, String minecraftUsername, String minecraftServer, boolean isHandled) {
        this.setId(id);
        this.setMinecraftUsername(minecraftUsername);
        this.setMinecraftServer(minecraftServer);
        this.setHandled(isHandled);
    }

    public int getId() {
        return id;
    }
    public boolean isHandled() {
        return isHandled;
    }
    public void setHandled(boolean isHandled) {
        this.isHandled = isHandled;
    }
    public String getMinecraftServer() {
        return minecraftServer;
    }
    public void setMinecraftServer(String minecraftServer) {
        this.minecraftServer = minecraftServer;
    }
    public String getMinecraftUsername() {
        return minecraftUsername;
    }
    public void setMinecraftUsername(String minecraftUsername) {
        this.minecraftUsername = minecraftUsername;
    }
    public void setId(int id) {
        this.id = id;
    }    
}
