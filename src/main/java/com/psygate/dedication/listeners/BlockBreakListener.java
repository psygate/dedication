package com.psygate.dedication.listeners;

import com.psygate.dedication.data.BlockBreakTarget;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class BlockBreakListener implements Listener {

    private final Map<UUID, Set<BlockBreakTarget>> incrementOn = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void consume(BlockBreakEvent ev) {
        if (ev.getPlayer() == null) {
            return;
        }
        if (incrementOn.containsKey(ev.getPlayer().getUniqueId())) {
            for (BlockBreakTarget tgt : incrementOn.get(ev.getPlayer().getUniqueId())) {
                if (tgt.isAcceptAny() || tgt.getMaterial() == ev.getBlock().getType()) {
                    tgt.increment(1);
                }
            }
        }
    }

    @EventHandler
    public void logout(PlayerQuitEvent ev) {
        removeTarget(ev.getPlayer().getUniqueId());
    }

    public void addTarget(BlockBreakTarget tgt) {
        incrementOn.putIfAbsent(tgt.getUUID(), new HashSet<BlockBreakTarget>());
        incrementOn.get(tgt.getUUID()).add(tgt);
    }

    public void removeTarget(BlockBreakTarget tgt) {
        incrementOn.remove(tgt.getUUID());
    }

    public void removeTarget(UUID uuid) {
        incrementOn.remove(uuid);
    }
}
