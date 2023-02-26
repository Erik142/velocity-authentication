package com.wahlberger.velocity.database;

import com.wahlberger.velocity.model.AuthenticationRequest;

/**
 * @author Erik Wahlberger
 */
public interface UserAuthenticationDatabase {
    void register(String minecraftUser, String registrationCode);
    int getAuthRequestId(int id);
    AuthenticationRequest getAuthRequest(int id);
    int addAuthRequest(String minecraftUser, String ipAddress, String minecraftServer);
    boolean isPlayerValid(String playerName);
    boolean isRegistrationCodeValid(String registrationCode);
    boolean isPlayerAuthValid(String playerName, String ipAddress);
    boolean isDatabaseAvailable();
}
