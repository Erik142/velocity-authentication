package com.wahlberger.velocity.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.wahlberger.velocity.database.ServerProtectionDatabase;
import com.wahlberger.velocity.database.UserAuthenticationDatabase;
import com.wahlberger.velocity.luckperms.AuthenticationContextCalculator;
import com.wahlberger.velocity.luckperms.MetadataHelper;
import com.wahlberger.velocity.model.AuthHelper;
import com.wahlberger.velocity.model.AuthenticatedUsers;
import com.wahlberger.velocity.model.Configuration;

import org.slf4j.Logger;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Optional;

import javax.annotation.Nullable;

import net.kyori.adventure.text.Component;

/**
 * @author Erik Wahlberger
 * @version 2021-12-28
 */
public class ServerPreconnectEventHandler {
    private static final long SLEEP_TIME_MILLIS = 3000;
    private static final String LOBBY_NAME = "lobby";
    private final UserAuthenticationDatabase userDatabase;
    private final ServerProtectionDatabase protectionDatabase;
    private final Logger logger;
    private final AuthHelper authHelper;

    public ServerPreconnectEventHandler(Logger logger, UserAuthenticationDatabase userDatabase,
            ServerProtectionDatabase protectionDatabase, Configuration configuration) {
        this.logger = logger;
        this.userDatabase = userDatabase;
        this.protectionDatabase = protectionDatabase;
        this.authHelper = new AuthHelper(logger, userDatabase, protectionDatabase, configuration);
    }

    /**
     * Handles the ServerPreConnectEvent.
     * 
     * @param event The ServerPreConnectEvent
     */
    @Subscribe
    public void onServerPreconnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getGameProfile().getName();
        RegisteredServer toServer = event.getOriginalServer();
        RegisteredServer fromServer = null;
        String ipAddress = "";
        InetAddress inetAddress = player.getRemoteAddress().getAddress();

        if (inetAddress instanceof Inet4Address) {
            ipAddress = ((Inet4Address)inetAddress).toString();
        } else {
            ipAddress = ((Inet6Address)inetAddress).toString();
        }

        if (authHelper.isUserDatabaseAuthAvailable() && userDatabase.isPlayerAuthValid(playerName, ipAddress)) {
            AuthenticatedUsers.setAuthenticated(playerName);
        }

        if (toServer.getServerInfo().getName().equals(LOBBY_NAME)) {
            logger.info(String.format("Connecting %s to the lobby", playerName));
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(toServer));
            return;
        } else {
            logger.info(
                    String.format("Player %s wants to connect to %s.", playerName, toServer.getServerInfo().getName()));
        }

        Optional<ServerConnection> connection = event.getPlayer().getCurrentServer();

        if (connection.isPresent()) {
            fromServer = connection.get().getServer();
            logger.info(String.format("Current server connection is %s for player %s.",
                    fromServer.getServerInfo().getName(), playerName));
        } else {
            logger.warn(String.format("Current server connection is not available for player %s.", playerName));
        }

        if (authHelper.isAuthRequired(fromServer, toServer, playerName)) {
            authHelper.authenticate(player, ipAddress, toServer);

            if (AuthenticatedUsers.isAuthenticated(playerName)) {
                authHelper.teleportUser(event, player, toServer);
            } else if (authHelper.isUserDatabaseAuthAvailable()) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            } else {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                player.sendMessage(Component.text(
                        "Server authentication failed. Please try again and contact a moderator if the issue persists."));
            }
        } else {
            authHelper.teleportUser(event, player, toServer);
        }

        /*
        if (fromServer != null && !fromServer.getServerInfo().getName().equals(LOBBY_NAME)
                && protectionDatabase.isServerAuthRequired(fromServer.getServerInfo().getName())) {
            AuthenticatedUsers.setAuthenticated(event.getPlayer().getUsername());
            logger.info(String.format(
                    "Player %s has authenticated through Discord and will now be transferred to the server %s.",
                    playerName, toServer.getServerInfo().getName()));
            fromServer.sendMessage(Component.text(String.format("Hello %s. Sending you to %s. Have a good time!",
                    playerName, toServer.getServerInfo().getName())));
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(toServer));
            return;
        }

        if (!protectionDatabase.isServerAuthRequired(toServer.getServerInfo().getName())) {
            if (userDatabase.isPlayerAuthValid(playerName)) {
                AuthenticatedUsers.setAuthenticated(playerName);
            }

            logger.info(String.format("Player %s does not have to authenticate for the server %s.", playerName,
                    toServer.getServerInfo().getName()));
            fromServer.sendMessage(Component.text(String.format("Hello %s. Sending you to %s. Have a good time!",
                    playerName, toServer.getServerInfo().getName())));
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(toServer));
            return;
        }

        if (!userDatabase.isPlayerAuthValid(playerName)) {
            logger.error(String.format("Player %s has not logged in in Discord.", playerName));
            if (fromServer != null) {
                userDatabase.addAuthRequest(playerName, toServer.getServerInfo().getName());
                fromServer.sendMessage(Component.text(
                        "You are currently not logged in to this server. I have sent you a login request on Discord. If that does not work, please use the command /login to login, then follow the instructions from the Discord bot."));
            }

            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        AuthenticatedUsers.setAuthenticated(event.getPlayer().getUsername());
        logger.info(String.format("Player %s has logged in in Discord and will now be transferred to the server %s.",
                playerName, toServer.getServerInfo().getName()));
        fromServer.sendMessage(Component.text(String.format("Hello %s. Sending you to %s. Have a good time!",
                playerName, toServer.getServerInfo().getName())));

        try {
            Thread.sleep(SLEEP_TIME_MILLIS);
        } catch (InterruptedException e) {
            logger.error(e.toString());
        }

        event.setResult(ServerPreConnectEvent.ServerResult.allowed(toServer));
        */
    }

    
}
