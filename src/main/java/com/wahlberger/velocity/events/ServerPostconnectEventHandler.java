package com.wahlberger.velocity.events;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Optional;


import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.wahlberger.velocity.database.ServerProtectionDatabase;
import com.wahlberger.velocity.database.UserAuthenticationDatabase;

import org.slf4j.Logger;

import net.kyori.adventure.text.Component;

public class ServerPostconnectEventHandler {
    public static final String STANDARD_SERVER = "survival";

    private final Logger logger;
    private final UserAuthenticationDatabase userDatabase;
    private final ServerProtectionDatabase protectionDatabase;
    private final ProxyServer proxyServer;

    public ServerPostconnectEventHandler(Logger logger, UserAuthenticationDatabase userDatabase, ServerProtectionDatabase protectionDatabase, ProxyServer proxyServer) {
        this.logger = logger;
        this.userDatabase = userDatabase;
        this.protectionDatabase = protectionDatabase;
        this.proxyServer = proxyServer;
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        this.logger.info(String.format("The player %s has connected to a server.", event.getPlayer().getUsername()));

        Player player = event.getPlayer();

        if (event.getPreviousServer() != null) {
            this.logger.info(String.format("The player %s was previously connected to a server.", event.getPlayer().getUsername()));
            return;
        }

        // Previous server is null, meaning that we have just connected to the proxy
        Optional<RegisteredServer> optionalServer = this.proxyServer.getServer(STANDARD_SERVER);

        if (!optionalServer.isPresent()) {
            logger.error(String.format("Cannot redirect player %s to the server %s. The server does not exist.", player.getUsername(), STANDARD_SERVER));
            return;
        }

        RegisteredServer server = optionalServer.get();

        String ipAddress = "";
        InetAddress inetAddress = player.getRemoteAddress().getAddress();

        if (inetAddress instanceof Inet4Address) {
            ipAddress = ((Inet4Address)inetAddress).toString();
        } else {
            ipAddress = ((Inet6Address)inetAddress).toString();
        }

        if (this.userDatabase.isPlayerAuthValid(player.getUsername(), ipAddress)) {
            player.sendMessage(Component.text(String.format("You are already authenticated!")));
            player.createConnectionRequest(server).fireAndForget();
        } else if (this.protectionDatabase.isServerAuthRequired(STANDARD_SERVER)) {
            if (userDatabase.isPlayerValid(player.getUsername())) {
                player.createConnectionRequest(server).fireAndForget();
            }
        }
    }
}
