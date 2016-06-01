package com.psygate.dedication.listeners;

import com.psygate.dedication.Dedication;
import isaac.bastion.Bastion;
import isaac.bastion.manager.BastionBlockManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Mr_Little_Kitty on 5/31/2016.
 */
public class BastionListener implements Listener
{
    private BastionBlockManager bastionManager;
    public BastionListener()
    {
        this.bastionManager = Bastion.getBastionManager();
    }

    //Stop players who are not dedicated from placing blocks in a bastion and damaging the bastion
    @EventHandler(ignoreCancelled=true, priority= EventPriority.LOWEST) //Priority is set to lowest so this event is triggered before the bastion event
    public void onBlockPlace(BlockPlaceEvent event)
    {
        //If the player is dedicated then return because we don't need to do anything
        if (Dedication.initPlayer(event.getPlayer().getUniqueId()).isDedicated())
            return;

        Set blocks = new CopyOnWriteArraySet();
        blocks.add(event.getBlock());

        //See if there are any bastions covering this location that the player is not on the groups for
        Set blocking = bastionManager.shouldStopBlock(null, blocks, event.getPlayer().getUniqueId());

        //We have already determined the player is not dedicated, so if there are blocking bastions
        //we cancel the event.
        if (blocking.size() != 0)
        {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED+"You must be dedicated to erode bastions");
        }
    }
}
