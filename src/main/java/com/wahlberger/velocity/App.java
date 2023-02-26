package com.wahlberger.velocity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.wahlberger.velocity.commands.LoginCommand;
import com.wahlberger.velocity.commands.RegisterCommand;
import com.wahlberger.velocity.commands.SetProtectedServerCommand;
import com.wahlberger.velocity.database.BinaryDatabase;
import com.wahlberger.velocity.database.PostgresDatabase;
import com.wahlberger.velocity.events.LoginEventHandler;
import com.wahlberger.velocity.events.ServerPostconnectEventHandler;
import com.wahlberger.velocity.events.ServerPreconnectEventHandler;
import com.wahlberger.velocity.luckperms.AuthenticationContextCalculator;
import com.wahlberger.velocity.model.Configuration;
import com.wahlberger.velocity.queue.RabbitMqMessageQueue;

import org.slf4j.Logger;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

/**
 * @author Erik Wahlberger
 * @version 2022-02-01
 * Plugin entrypoint
 */
@Plugin(
    id = "velocity-authentication", 
    name = "Velocity authentication", 
    version = "0.0.1", 
    authors = { "Erik Wahlberger" },
    dependencies = {
        @Dependency(id = "luckperms")
    }
  )
public final class App {
    private ProxyServer server;
    private Logger logger;
    private final PostgresDatabase userDatabase;
    private final BinaryDatabase protectionDatabase;
    private final Configuration config;

    @Inject
    public App(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) throws ClassNotFoundException, SQLException, IOException, TimeoutException {
        this.server = server;
        this.logger = logger;

        Path configPath = dataDirectory.resolve("config.json");
        Path serverProtectionPath = dataDirectory.resolve("servers.bin");
        this.config = Configuration.getJsonConfig(configPath);
        this.userDatabase = new PostgresDatabase(this.server, this.logger, config.getPostgresConfiguration());
        this.protectionDatabase = new BinaryDatabase(serverProtectionPath.toString(), logger);
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        luckPerms.getContextManager().registerCalculator(new AuthenticationContextCalculator());
        this.server.getEventManager().register(this, new LoginEventHandler());
        this.server.getEventManager().register(this, new ServerPreconnectEventHandler(this.logger, this.userDatabase, this.protectionDatabase, this.config));
        this.server.getEventManager().register(this, new ServerPostconnectEventHandler(this.logger, this.userDatabase, this.protectionDatabase, this.server));

        CommandMeta loginMeta = this.server.getCommandManager().metaBuilder("login").build();
        CommandMeta registerMeta = this.server.getCommandManager().metaBuilder("register").build();
        CommandMeta serverProtectionMeta = this.server.getCommandManager().metaBuilder("serverProtection").build();
        this.server.getCommandManager().register(loginMeta, new LoginCommand(this.logger, this.userDatabase, this.protectionDatabase, this.server, this.config));
        this.server.getCommandManager().register(registerMeta, new RegisterCommand(this.logger, this.userDatabase, this.server));
        this.server.getCommandManager().register(serverProtectionMeta, new SetProtectedServerCommand(this.logger, this.userDatabase, this.protectionDatabase, this.server.getConfiguration()));
    }
}
