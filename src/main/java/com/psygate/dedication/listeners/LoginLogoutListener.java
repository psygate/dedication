package com.psygate.dedication.listeners;

import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.BlockPlaceTarget;
import com.psygate.dedication.data.EdibleTarget;
import com.psygate.dedication.data.PlayerData;
import com.psygate.dedication.data.TimeTarget;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Achievement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class LoginLogoutListener implements Listener {

    private Map<UUID, Long> loginTimes = new HashMap<>();
    private final Map<UUID, Set<TimeTarget>> incrementOn = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerJoinEvent ev) {
        PlayerData data = Dedication.initPlayer(ev.getPlayer().getUniqueId());
        if (data.isDedicated()) {
            ev.getPlayer().awardAchievement(Achievement.END_PORTAL);
        }
        loginTimes.put(ev.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogout(PlayerQuitEvent ev) {
        if (incrementOn.containsKey(ev.getPlayer().getUniqueId())) {
            for (TimeTarget tgt : incrementOn.get(ev.getPlayer().getUniqueId())) {
                tgt.increment(System.currentTimeMillis() - loginTimes.get(ev.getPlayer().getUniqueId()));
            }
        }

        incrementOn.remove(ev.getPlayer().getUniqueId());
        loginTimes.remove(ev.getPlayer().getUniqueId());
        Dedication.saveAndRemovePlayer(ev.getPlayer().getUniqueId());
    }

    public void addTarget(TimeTarget tgt) {
        incrementOn.putIfAbsent(tgt.getUUID(), new HashSet<TimeTarget>());
        incrementOn.get(tgt.getUUID()).add(tgt);
    }

    public void removeTarget(TimeTarget tgt) {
        incrementOn.remove(tgt.getUUID());
    }

    public void removeTarget(UUID uuid) {
        incrementOn.remove(uuid);
    }
}
