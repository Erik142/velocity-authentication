package com.wahlberger.velocity.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.config.ProxyConfig;
import com.wahlberger.velocity.database.ServerProtectionDatabase;
import com.wahlberger.velocity.database.UserAuthenticationDatabase;

import org.slf4j.Logger;

import net.kyori.adventure.text.Component;

public class SetProtectedServerCommand implements SimpleCommand {
    private static final String LOBBY_NAME = "lobby";
    private static final String PERMISSION_NAME = "auth.protect";

    private final UserAuthenticationDatabase userDatabase;
    private final ServerProtectionDatabase protectionDatabase;
    private final Logger logger;
    private final ProxyConfig proxyConfig;

    public SetProtectedServerCommand(Logger logger, UserAuthenticationDatabase userDatabase, ServerProtectionDatabase protectionDatabase, ProxyConfig proxyConfig) {
        this.logger = logger;
        this.userDatabase = userDatabase;
        this.protectionDatabase = protectionDatabase;
        this.proxyConfig = proxyConfig;
    }

    @Override
    public void execute(Invocation invocation) {
        String name = "";

        if (invocation.arguments().length == 0) {
            if (invocation.source() instanceof Player) {
                Player player = (Player) invocation.source();
                Optional<ServerConnection> optionalConnection = player.getCurrentServer();

                if (optionalConnection.isPresent()) {
                    name = optionalConnection.get().getServerInfo().getName();
                }
            }
        } else {
            name = invocation.arguments()[0];
            if (!proxyConfig.getServers().keySet().contains(name)) {
                invocation.source().sendMessage(Component.text(String.format("%s is not a valid server name.", name)));
                return;
            }
        }

        if (name.equals(LOBBY_NAME)) {
            invocation.source().sendMessage(Component.text("Cannot change server protection settings for the lobby."));
            return;
        }

        if (this.protectionDatabase.isServerAuthRequired(name)) {
            this.protectionDatabase.deleteServerAuth(name);
            invocation.source().sendMessage(
                    Component.text(String.format("Server protection has been disabled for the server %s.", name)));
        } else {
            this.protectionDatabase.addServerAuth(name);
            invocation.source().sendMessage(
                    Component.text(String.format("Server protection has been enabled for the server %s.", name)));
        }
    }

    @Override
    public List<String> suggest(final Invocation source) {
        List<String> suggestions = new ArrayList<>();
        String prefix = "";

        if (source.arguments().length == 1) {
            prefix = source.arguments()[0];
        }
        else if (source.arguments().length > 1) {
            return suggestions;
        }

        for (String server: this.proxyConfig.getServers().keySet()) {
            if (server.startsWith(prefix)) {
                suggestions.add(server);
            }
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().getPermissionValue(PERMISSION_NAME).asBoolean();    
    }
}
