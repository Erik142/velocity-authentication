package com.wahlberger.velocity.luckperms;

import javax.annotation.Nonnull;

import com.velocitypowered.api.proxy.Player;
import com.wahlberger.velocity.model.AuthenticatedUsers;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;

public class AuthenticationContextCalculator implements ContextCalculator<Player> {
    public static final String KEY = "authenticated";

    @Override
    public void calculate(@Nonnull Player player, @Nonnull ContextConsumer contextConsumer) {
        contextConsumer.accept(KEY, String.valueOf(AuthenticatedUsers.isAuthenticated(player.getUsername())));
    }
    
    @Override
    public ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(KEY, "true")
                .add(KEY, "false")
                .build();
    }
}
