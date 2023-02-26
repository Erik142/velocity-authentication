package com.wahlberger.velocity.model;

public class ServerConnectModel extends QueueModel {
    private static final String CONNECT_COMMAND = "connect";

    public ServerConnectModel(String user, String server) {
        super(user, CONNECT_COMMAND, server);
    }
}
