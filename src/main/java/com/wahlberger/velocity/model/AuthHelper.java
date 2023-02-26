package com.wahlberger.velocity.model;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.wahlberger.velocity.commands.LoginCommand;
import com.wahlberger.velocity.database.ServerProtectionDatabase;
import com.wahlberger.velocity.database.UserAuthenticationDatabase;

import org.slf4j.Logger;

import net.kyori.adventure.text.Component;

public class AuthHelper {
    private final UserAuthenticationDatabase userDatabase;
    private final ServerProtectionDatabase protectionDatabase;
    private final Logger logger;
    private final Configuration config;

    private final long LoginTimeout = 30000;

    private boolean isAuthenticated = false;
    private boolean isLoginCommandCompleted = false;

    public AuthHelper(Logger logger, UserAuthenticationDatabase userDatabase,
            ServerProtectionDatabase protectionDatabase, Configuration config) {
        this.logger = logger;
        this.userDatabase = userDatabase;
        this.protectionDatabase = protectionDatabase;
        this.config = config;
    }

    public boolean isAuthRequired(RegisteredServer fromServer, RegisteredServer toServer, String playerName) {
        if (AuthenticatedUsers.isAuthenticated(playerName)) {
            logger.info(String.format("Player %s is already authenticated", playerName));
            return false;
        }

        if (!protectionDatabase.isServerAuthRequired(toServer.getServerInfo().getName())) {
            logger.info("Player %s does not have to authenticate to connect to the server %s", playerName,
                    toServer.getServerInfo().getName());
            return false;
        }

        logger.info("Player %s is not authenticated and is trying to connect to the protected server %s", playerName,
                toServer.getServerInfo().getName());
        return true;
    }

    public void authenticate(Player player, String ipAddress, @Nullable RegisteredServer server) {
        logger.info(String.format("Starting authentication for the player %s", player.getGameProfile().getName()));

        if (isUserDatabaseAuthAvailable()) {
            authenticateDiscord(player, ipAddress, server);

        } else {
            authenticatePassword(player, player.getGameProfile().getName(), server, null, false, false);
        }
    }

    public void authenticateDiscord(Player player, String ipAddress, @Nullable RegisteredServer server) {
        if (isUserDatabaseAuthAvailable()) {
            if (!userDatabase.isPlayerValid(player.getGameProfile().getName())) {
                logger.error(
                        String.format("Player %s is not registered in Discord.", player.getGameProfile().getName()));
                player.sendMessage(Component.text(
                        "You are currently not a registered user on this server. Please use the command /register in Discord to register your Minecraft user."));

                return;
            }
            logger.info(String.format(
                    "User database is available! Trying to authenticate the player %s through the user database",
                    player.getGameProfile().getName()));
            String serverName = "";

            if (server != null) {
                serverName = server.getServerInfo().getName();
            }

            userDatabase.addAuthRequest(player.getGameProfile().getName(), ipAddress, serverName);
        }
    }

    public void authenticatePassword(Player player, String playerToAuthenticate, @Nullable RegisteredServer server,
            @Nullable String password, boolean isCommand, boolean isOtherPlayer) {
        logger.warn(String.format(
                "User database is not available! Trying to authenticate the player %s using password protection",
                player.getGameProfile().getName()));

        boolean hasPermission = false;

        if (isOtherPlayer) {
            hasPermission = player.getPermissionValue(LoginCommand.LoginOthersPasswordPermission).asBoolean();
        } else {
            hasPermission = player.getPermissionValue(LoginCommand.LoginSelfPasswordPermission).asBoolean();
        }

        if (!hasPermission) {
            logger.error(String.format(
                    "The player %s does not have permission to authenticate using password protection",
                    player.getGameProfile().getName()));
            player.sendMessage(
                    Component.text("You do not have the permission to authenticate using password protection."));
        } else {
            if (!isCommand) {
                player.sendMessage(Component.text(
                        "Discord authentication is currently not available. Please login with the command /login password <enter server password here>. I will wait for the next 30 seconds before timing out."));
                LoginCommand.registerResultListener(player.getGameProfile().getName(), new LoginResultHandler() {
                    @Override
                    public void handle(String playerName, boolean authenticated) {
                        isAuthenticated = authenticated;
                        isLoginCommandCompleted = true;
                    }
                });

                Instant start = Instant.now();

                while (!isLoginCommandCompleted) {
                    Instant current = Instant.now();

                    long timeElapsed = Duration.between(start, current).toMillis();

                    if (timeElapsed >= LoginTimeout) {
                        player.sendMessage(Component.text("The login request timed out. Please try again."));
                        return;
                    }
                }

                LoginCommand.unregisterResultListener(player.getGameProfile().getName());

                if (!isAuthenticated) {
                    player.sendMessage(Component
                            .text("The entered password was incorrect. Please try again using the /login command."));
                    return;
                }
            } else {
                if (password == null || password.trim().equals("")) {
                    player.sendMessage(Component.text("No password was specified. Please try again."));
                    return;
                }

                logger.info(String.format("The correct password is '%s'", config.getServerPassword()));
                if (!password.equals(config.getServerPassword())) {
                    player.sendMessage(Component
                            .text("The entered password was incorrect. Please try again using the /login command."));
                    return;
                }
            }

            AuthenticatedUsers.setAuthenticated(playerToAuthenticate);
        }
    }

    public boolean isUserDatabaseAuthAvailable() {
        return userDatabase.isDatabaseAvailable();
    }

    public void teleportUser(ServerPreConnectEvent event, Player player, RegisteredServer server) {
        player.sendMessage(Component
                .text(String.format("Sending you to %s now. Have fun!", server.getServerInfo().getName())));
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.error(e.toString());
            player.sendMessage(Component.text(String.format(
                    "An error occured while forwarding you to the server %s. Please try again manually using the command /server %s",
                    server.getServerInfo().getName(), server.getServerInfo().getName())));
            return;
        }

        event.setResult(ServerPreConnectEvent.ServerResult.allowed(server));
    }
}
