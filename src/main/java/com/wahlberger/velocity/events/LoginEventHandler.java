package com.wahlberger.velocity.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.wahlberger.velocity.model.AuthenticatedUsers;

public class LoginEventHandler {
   @Subscribe
   public void onLogin(LoginEvent event) {
       Player player = event.getPlayer();
       AuthenticatedUsers.unsetAuthenticated(player.getUsername());
   } 
}
