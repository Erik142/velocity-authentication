package com.wahlberger.velocity.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
public class Configuration {
    private PostgresConfiguration postgresConfiguration;
    private RabbitMqConfiguration rabbitMqConfiguration;
    private String serverPassword;

    public static Configuration getJsonConfig(Path jsonPath) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader jsonReader = new JsonReader(new FileReader(jsonPath.toFile()));

        Configuration config = gson.fromJson(jsonReader, Configuration.class);
        return config;
    }

    public PostgresConfiguration getPostgresConfiguration() {
        return this.postgresConfiguration;
    }

    public RabbitMqConfiguration getRabbitMqConfiguration() {
        return this.rabbitMqConfiguration;
    }

    public String getServerPassword() {
        return this.serverPassword;
    }

    public void setServerPassword(String password) {
        this.serverPassword = password;
    }
}
