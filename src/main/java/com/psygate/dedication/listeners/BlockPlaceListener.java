package com.psygate.dedication.listeners;

import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.BlockBreakTarget;
import com.psygate.dedication.data.BlockPlaceTarget;
import com.psygate.dedication.data.EdibleTarget;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class BlockPlaceListener implements Listener {

    private final Map<UUID, Set<BlockPlaceTarget>> incrementOn = new HashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void consume(BlockPlaceEvent ev) {
        if (ev.getPlayer() == null) {
            return;
        }
        if (incrementOn.containsKey(ev.getPlayer().getUniqueId())) {
            for (BlockPlaceTarget tgt : incrementOn.get(ev.getPlayer().getUniqueId())) {
                if (tgt.isAcceptAny() || tgt.getMaterial() == ev.getBlock().getType()) {
                    tgt.increment(1);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void bucketEmpty(PlayerBucketEmptyEvent ev) {
        if (ev.getBucket() == Material.LAVA_BUCKET) {
            ev.getPlayer().sendMessage(Dedication.PREFIX + ChatColor.RED + " You cannot use this.");
            ev.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void igniteBlock(BlockIgniteEvent ev) {
        if (ev.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            Block down = ev.getBlock().getRelative(BlockFace.DOWN);
            if (down != null && down.getType() != Material.OBSIDIAN) {
                if (ev.getPlayer() != null) {
                    ev.getPlayer().sendMessage(Dedication.PREFIX + ChatColor.RED + " You cannot ignite this.");
                }
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void logout(PlayerQuitEvent ev) {
        removeTarget(ev.getPlayer().getUniqueId());
    }

    public void addTarget(BlockPlaceTarget tgt) {
        incrementOn.putIfAbsent(tgt.getUUID(), new HashSet<BlockPlaceTarget>());
        incrementOn.get(tgt.getUUID()).add(tgt);
    }

    public void removeTarget(BlockPlaceTarget tgt) {
        incrementOn.remove(tgt.getUUID());
    }

    public void removeTarget(UUID uuid) {
        incrementOn.remove(uuid);
    }
}
