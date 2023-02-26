package com.wahlberger.velocity.model;

public interface LoginResultHandler {
    void handle(String player, boolean isAuthenticated);    
}
