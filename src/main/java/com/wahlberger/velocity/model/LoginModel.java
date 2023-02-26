package com.wahlberger.velocity.model;

public class LoginModel extends QueueModel {
    private static final String LOGIN_COMMAND = "login";
    
    public LoginModel(String user) {
        super(user, LOGIN_COMMAND);
    }

    public LoginModel(String user, String server) {
        super(user, LOGIN_COMMAND, server);
    }
}
