package com.wahlberger.velocity.model;

import java.util.HashSet;
import java.util.Set;

public class AuthenticatedUsers {
    private static final Set<String> users = new HashSet<>();

    public static boolean isAuthenticated(String name) {
        return users.contains(name);
    }

    public static void unsetAuthenticated(String name) {
        if (users.contains(name)) {
            users.remove(name);
        }
    }

    public static void setAuthenticated(String name) {
        if (!users.contains(name)) {
            users.add(name);
        }
    }
}
