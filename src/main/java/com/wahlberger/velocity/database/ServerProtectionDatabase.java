package com.wahlberger.velocity.database;

public interface ServerProtectionDatabase {
    boolean isServerAuthRequired(String serverName);
    void deleteServerAuth(String serverName);
    void addServerAuth(String serverName);
}
