package com.psygate.dedication.listeners;

import com.psygate.dedication.data.EdibleTarget;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class EdibleListener implements Listener {

    private final Map<UUID, Set<EdibleTarget>> incrementOn = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void consume(PlayerItemConsumeEvent ev) {
        if (incrementOn.containsKey(ev.getPlayer().getUniqueId())) {
            for (EdibleTarget tgt : incrementOn.get(ev.getPlayer().getUniqueId())) {
                if (tgt.isAcceptAny() || tgt.getMaterial() == ev.getItem().getType()) {
                    tgt.increment(1);
                }
            }
        }
    }

    @EventHandler
    public void logout(PlayerQuitEvent ev) {
        removeTarget(ev.getPlayer().getUniqueId());
    }

    public void addTarget(EdibleTarget tgt) {
        incrementOn.putIfAbsent(tgt.getUUID(), new HashSet<EdibleTarget>());
        incrementOn.get(tgt.getUUID()).add(tgt);
    }

    public void removeTarget(EdibleTarget tgt) {
        incrementOn.remove(tgt.getUUID());
    }

    public void removeTarget(UUID uuid) {
        incrementOn.remove(uuid);
    }
}
