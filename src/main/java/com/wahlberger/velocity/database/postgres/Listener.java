package com.wahlberger.velocity.database.postgres;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.wahlberger.velocity.database.PostgresDatabase;
import com.wahlberger.velocity.model.AuthenticatedUsers;
import com.wahlberger.velocity.model.AuthenticationRequest;

import org.slf4j.Logger;

import net.kyori.adventure.text.Component;

import org.apache.commons.dbcp2.BasicDataSource;
import org.postgresql.PGConnection;

public class Listener extends Thread {
    private final BasicDataSource connectionPool;
    private final Logger logger;
    private final PostgresDatabase database;
    private final ProxyServer proxyServer;

    public Listener(BasicDataSource connectionPool, PostgresDatabase database, ProxyServer proxyServer, Logger logger) throws SQLException {
        this.connectionPool = connectionPool;
        this.database = database;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Connection conn = connectionPool.getConnection();
                PGConnection pgconn = conn.unwrap(org.postgresql.PGConnection.class);
                Statement stmt = conn.createStatement();
                stmt.execute("LISTEN approved_auths");
                stmt.close();

                while (true) {
                    org.postgresql.PGNotification notifications[] = pgconn.getNotifications();

                    // If this thread is the only one that uses the connection, a timeout can be
                    // used to
                    // receive notifications immediately:
                    // org.postgresql.PGNotification notifications[] =
                    // pgconn.getNotifications(10000);

                    if (notifications != null) {
                        for (int i = 0; i < notifications.length; i++) {
                            String idString = notifications[i].getParameter();
                            int id = Integer.parseInt(idString);
                            System.out.println("Got notification: " + idString);
                            this.handleNotification(id);
                        }
                    }

                    // wait a while before checking again for new
                    // notifications

                    Thread.sleep(500);
                }
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

            logger.error("An error occured while retrieving notifications from PostgreSQL. Trying to reconnect...");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleNotification(int authId) {
        Player player = null;
        try {
            logger.info("Received message from message queue!");
            int authRequestId = database.getAuthRequestId(authId);
            AuthenticationRequest authRequest = database.getAuthRequest(authRequestId);

            String playerName = authRequest.getMinecraftUsername();
            String serverName = authRequest.getMinecraftServer();


            Optional<Player> optionalPlayer = proxyServer.getPlayer(playerName);

            if (!optionalPlayer.isPresent()) {
                logger.error(String.format("The player %s cannot be found on the server.", playerName));
                return;
            }

            player = optionalPlayer.get();
            AuthenticatedUsers.setAuthenticated(playerName);

            if (serverName.trim().equals("")) {
                logger.info(String.format("The user %s did not request a server to be teleported to.", playerName));
                player.sendMessage(Component.text("You have successfully logged in! Use /server to choose which server to connect to."));
                return;
            }

            Optional<RegisteredServer> optionalServer = proxyServer.getServer(serverName);

            if (!optionalServer.isPresent()) {
                logger.error(String.format("The server %s cannot be found. The user %s will not be transferred.", serverName, playerName));
                player.sendMessage(Component.text(String.format("The server %s cannot be found. You will not be transferred.", serverName)));
                return;
            }

            RegisteredServer server = optionalServer.get();
            player.createConnectionRequest(server).fireAndForget();
        } catch (Exception e) {
            logger.error(String.format("An error occured when consuming data from RabbitMQ: %s", e.toString()));
            // TODO: Send message to player if player is not null
        }
    }
}
