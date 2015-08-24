/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication.listeners;

import com.wezro.dedication.Dedication;
import static com.wezro.dedication.Dedication.privilegeTokenKey;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.MetadataValue;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.permission.PermissionType;

/**
 *
 * @author florian
 */
public class CitadelListener implements Listener {
//Already handled by citadel prevention method.
//    @EventHandler(priority = EventPriority.HIGHEST)
//    public void onBlockBreak(BlockBreakEvent event) {
//        if (event.getPlayer() == null || Dedication.isPlayerDedicated((Player) event.getPlayer())) {
//            return;
//        }
//
//        if (Citadel.getReinforcementManager().isReinforced(event.getBlock())) {
//            Reinforcement rf = Citadel.getReinforcementManager().getReinforcement(event.getBlock());
//
//            if (rf instanceof PlayerReinforcement) {
//                PlayerReinforcement prf = (PlayerReinforcement) rf;
//
//                if (!prf.isBypassable(event.getPlayer())) {
//                    event.setCancelled(true);
//                    event.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + "You may not break this reinforcement.");
//                }
//            }
//        }
//    }

    @EventHandler
    public void citadelPrevention(ReinforcementDamageEvent event) {
        if (event.getPlayer() == null || Dedication.isPlayerDedicated((Player) event.getPlayer())) {
            return;
        }

        if (Citadel.getReinforcementManager().isReinforced(event.getBlock())) {
            Reinforcement rf = Citadel.getReinforcementManager().getReinforcement(event.getBlock());

            if (rf instanceof PlayerReinforcement) {
                PlayerReinforcement prf = (PlayerReinforcement) rf;
                if (!prf.isBypassable(event.getPlayer())) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + "You may not break this reinforcement.");
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (Dedication.isPlayerDedicated((Player) event.getPlayer())) {
            return;
        }
        if (event.getClickedBlock() == null || !(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            // Skip this. Player didnt click a block.
            return;
        }

        if (Dedication.getConfiguration().getLockedBlocks().contains(event.getClickedBlock().getType())
                && Citadel.getReinforcementManager().isReinforced(event.getClickedBlock())) {

            Reinforcement rf = Citadel.getReinforcementManager().getReinforcement(event.getClickedBlock());

            if (rf instanceof PlayerReinforcement) {
                PlayerReinforcement prf = (PlayerReinforcement) rf;
                if (!prf.isBypassable(event.getPlayer()) && !prf.isAccessible(event.getPlayer(), PermissionType.MEMBERS)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + "You may not open this container.");
                }
            }
        }
    }
}
