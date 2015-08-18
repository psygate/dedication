package com.wezro.dedication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Dedication extends JavaPlugin implements Listener {

    private PlayerDedicationBackend backend;
    private final String privilegeTokenKey = "DEDICATION_PRIVILEGE";
    private final String playedTimeTokenKey = "SERVER_TIME";
    private final Set<Material> lockedBlocks = new HashSet<>();

    @Override
    public void onEnable() {
        //Listen for all the events in this class
        getServer().getPluginManager().registerEvents(this, this);
        //Save out configration if it doesnt exist.
        getConfig().options().copyDefaults(true);
        getConfig().options().copyHeader(true);
        saveConfig();

        List<String> locked = (List<String>) getConfig().getList("lockedBlocks");
        try {
            for (String lock : locked) {
                lockedBlocks.add(Material.getMaterial(lock.replace(" ", "_").toUpperCase()));
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Dedication error. Loaded default locks.", e);

            lockedBlocks.add(Material.CHEST);
            lockedBlocks.add(Material.ENDER_CHEST);
            lockedBlocks.add(Material.TRAPPED_CHEST);
            lockedBlocks.add(Material.HOPPER);
            lockedBlocks.add(Material.FURNACE);
            lockedBlocks.add(Material.DROPPER);
            lockedBlocks.add(Material.DISPENSER);
        }
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        // Store some temp data in the player, which is the systems time.
        event.getPlayer().setMetadata(playedTimeTokenKey, new FixedMetadataValue(this, System.currentTimeMillis()));
        final Player player = event.getPlayer();
        //If the player doesn't have a place in the config, create one.
        if (!backend.hasPlayer(player)) {
            backend.addPlayer(player);
        }

        if (backend.getPlayerPlaytime(player) > TimeUnit.HOURS.toMillis(getConfig().getInt("minimumHoursPlayed"))) {
            event.getPlayer().setMetadata(privilegeTokenKey, new FixedMetadataValue(this, true));
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        //Check if the player has the metadata.
        if (event.getPlayer().hasMetadata(playedTimeTokenKey)) {
            List<MetadataValue> timestamp = event.getPlayer().getMetadata(playedTimeTokenKey);
            for (MetadataValue value : timestamp) {
                if (value.getOwningPlugin() == this) {
                    backend.addPlaytime(event.getPlayer(), System.currentTimeMillis() - value.asLong());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getEntity();
            for (MetadataValue value : attacker.getMetadata(privilegeTokenKey)) {
                if (value.getOwningPlugin() == this && value.asBoolean()) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            // Skip this. Player didnt click a block.
            return;
        }
        if (lockedBlocks.contains(event.getClickedBlock().getType())
                && Citadel.getReinforcementManager().isReinforced(event.getClickedBlock())) {
            Player player = (Player) event.getPlayer();

            Reinforcement rf = Citadel.getReinforcementManager().getReinforcement(event.getClickedBlock());

            if (rf instanceof PlayerReinforcement) {
                PlayerReinforcement prf = (PlayerReinforcement) rf;
                if (!prf.isAccessible(event.getPlayer(), PermissionType.MEMBERS) || !player.hasMetadata(privilegeTokenKey)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("dtl")) {
            //Send the player the time they played
            sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "[Dedication]" + ChatColor.RESET
                    + "You have played:" + getTime((Player) sender));
            return true;
        }
        return false;
    }

    //Simple little function to check how much time the player has left.
    public long getTime(Player player) {
        if (player.hasMetadata(playedTimeTokenKey)) {

            long joinTime = player.getMetadata(playedTimeTokenKey).get(0).asLong();
            long now = System.currentTimeMillis();
            long hours = (joinTime - now) / (3600000) % 24;
            long hoursPrev = (long) this.getConfig().get(player.getUniqueId() + ".hoursPlayed");

            return hours + hoursPrev;
        } else {
            return 0;
        }
    }
}
