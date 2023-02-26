package com.wahlberger.velocity.queue;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.wahlberger.velocity.model.RabbitMqConfiguration;
import com.wahlberger.velocity.model.ServerConnectModel;

import org.slf4j.Logger;

import net.kyori.adventure.text.Component;

public class RabbitMqMessageQueue implements MessageQueue {
    private final Logger logger;
    private final ProxyServer proxyServer;
    private final RabbitMqConfiguration configuration;

    private Channel publishChannel, consumeChannel;

    public RabbitMqMessageQueue(Logger logger, ProxyServer proxyServer, RabbitMqConfiguration configuration) {
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.configuration = configuration;
    }

    public void openConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(configuration.getUsername());
        factory.setPassword(configuration.getPassword());
        factory.setVirtualHost(configuration.getVirtualHost());
        factory.setHost(configuration.getHost());
        factory.setPort(configuration.getPort());
        factory.setAutomaticRecoveryEnabled(true);

        Connection publishConnection = factory.newConnection();
        Connection consumeConnection = factory.newConnection();
        this.publishChannel = publishConnection.openChannel().get();
        this.consumeChannel = consumeConnection.openChannel().get();
        this.consumeChannel.basicConsume(configuration.getConsumeQueueName(), true, "*",
            new DefaultConsumer(this.consumeChannel) {
                @Override
                public void handleDelivery(String consumerTag,
                                        Envelope envelope,
                                        AMQP.BasicProperties properties,
                                        byte[] body)
                {
                    Player player = null;
                    try {
                        logger.info("Received message from message queue!");
                        String content = new String(body);

                        Gson gson = new Gson();

                        ServerConnectModel connectModel = gson.fromJson(content, ServerConnectModel.class);

                        String playerName = connectModel.getUser();
                        String serverName = connectModel.getServer();

                        Optional<Player> optionalPlayer = proxyServer.getPlayer(playerName);

                        if (!optionalPlayer.isPresent()) {
                            logger.error(String.format("The player %s cannot be found on the server.", playerName));
                            return;
                        }

                        player = optionalPlayer.get();

                        Optional<RegisteredServer> optionalServer = proxyServer.getServer(serverName);

                        if (!optionalServer.isPresent()) {
                            logger.error(String.format("The server %s cannot be found. The user %s will not be transferred.", serverName, playerName));
                            player.sendMessage(Component.text(String.format("The server %s cannot be found. You will not be transferred.", serverName)));
                            return;
                        }

                        RegisteredServer server = optionalServer.get();

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            logger.error(e.toString());
                        }

                        player.createConnectionRequest(server).fireAndForget();
                    } catch (Exception e) {
                        logger.error(String.format("An error occured when consuming data from RabbitMQ: %s", e.toString()));
                        // TODO: Send message to player if player is not null
                    }
                }
            });
    }

    @Override
    public void sendMessage(String message) {
        try {
            this.publishChannel.basicPublish(configuration.getPublishExchangeName(), configuration.getRoutingKey(), null, message.getBytes());
        } catch (IOException e) {
            logger.error(e.toString());
        }
    }
    
}
