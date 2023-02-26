package com.wahlberger.velocity.luckperms;

import com.velocitypowered.api.proxy.Player;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;

public class MetadataHelper {
    public static void setMetadata(Player player, String key, String value) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        MetaNode metaNode = MetaNode.builder(key, value).build();

        User user = luckPerms.getPlayerAdapter(Player.class).getUser(player);
        user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals(key)));
        user.data().add(metaNode);
        luckPerms.getUserManager().saveUser(user);
    }

    public static String getMetadataValue(Player player, String key) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        CachedMetaData metadata = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
        String value = metadata.getMetaValue(key);

        return value;
    }
}
