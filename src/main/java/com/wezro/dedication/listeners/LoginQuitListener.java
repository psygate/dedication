/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication.listeners;

import com.wezro.dedication.Dedication;
import static com.wezro.dedication.Dedication.playedTimeTokenKey;
import static com.wezro.dedication.Dedication.privilegeTokenKey;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

/**
 *
 * @author florian
 */
public class LoginQuitListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        // Store some temp data in the player, which is the systems time.
        event.getPlayer().setMetadata(playedTimeTokenKey, new FixedMetadataValue(Dedication.getInstance(), System.currentTimeMillis()));
        final Player player = event.getPlayer();
        //If the player doesn't have a place in the config, create one.
        if (!Dedication.hasPlayer(player)) {
            Dedication.addPlayer(player);
        }

        if (Dedication.getPlayerPlaytime(player) > TimeUnit.HOURS.toMillis(Dedication.getConfiguration().getHoursRequired())) {
            Dedication.getInstance().getLogger().log(Level.INFO, "Player {0}({1}) has dedicated status.", new Object[]{event.getPlayer().getName(), event.getPlayer().getUniqueId()});
            event.getPlayer().setMetadata(privilegeTokenKey, new FixedMetadataValue(Dedication.getInstance(), true));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        //Check if the player has the metadata.
        if (event.getPlayer().hasMetadata(playedTimeTokenKey)) {
            List<MetadataValue> timestamp = event.getPlayer().getMetadata(playedTimeTokenKey);
            for (MetadataValue value : timestamp) {
                if (value.getOwningPlugin() == this) {
                    Dedication.addPlaytime(event.getPlayer(), System.currentTimeMillis() - value.asLong());
                    break;
                }
            }
        }
    }
}
