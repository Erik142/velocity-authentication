package com.wahlberger.velocity.commands;

import java.util.Optional;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.wahlberger.velocity.database.UserAuthenticationDatabase;
import com.wahlberger.velocity.events.ServerPostconnectEventHandler;

import org.slf4j.Logger;

import net.kyori.adventure.text.Component;

public class RegisterCommand implements SimpleCommand {
    private final Logger logger;
    private final UserAuthenticationDatabase database;
    private final ProxyServer proxyServer;

    public RegisterCommand(Logger logger, UserAuthenticationDatabase database, ProxyServer proxyServer) {
        this.logger = logger;
        this.database = database;
        this.proxyServer = proxyServer;
    }

    @Override
    public void execute(Invocation invocation) {
        String name = "";
        String registrationCode = "";
        Player player = null;

        if (invocation.arguments().length == 1) {
            if (invocation.source() instanceof Player) {
                player = (Player) invocation.source();
                name = player.getGameProfile().getName();
            }

            registrationCode = invocation.arguments()[0];
        } else {
            logger.info(String.format("%d arguments provided to the register command.", invocation.arguments().length));
            invocation.source().sendMessage(Component.text("The /register command takes one argument: The registration code that you received from the Discord bot. Try again with the registration code as a parameter."));
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("You have");

        if (!database.isPlayerValid(name)) {
            if (!database.isRegistrationCodeValid(registrationCode)) {
                logger.info(String.format("The user %s tried to register with the invalid registration code %s.", name, registrationCode));
                messageBuilder.append(" provided an invalid registration code. Please use the /register command in Discord to retrieve a valid registration code.");
            } else {
                logger.info(String.format("The user %s has entered a valid registration code. Finalizing registration...", name));

                database.register(name, registrationCode);

                messageBuilder.append(
                        " been successfully registered. I will now send you a login request on Discord to be able to send you the survival server. Check your Discord DMs!");
                invocation.source().sendMessage(Component.text(messageBuilder.toString()));

                Optional<RegisteredServer> possibleServer = proxyServer.getServer(ServerPostconnectEventHandler.STANDARD_SERVER);
                if (!possibleServer.isPresent()) {
                    invocation.source().sendMessage(Component.text(String.format("The server %s is not a valid server. Please try to connect to a server manually using the /server command.")));
                } else {
                    RegisteredServer server = possibleServer.get();
                    player.createConnectionRequest(server).fireAndForget(); 
                }
                return;
            }
        }
    }

}
