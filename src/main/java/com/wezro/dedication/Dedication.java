package com.wezro.dedication;

import com.wezro.dedication.backend.FileBackend;
import com.wezro.dedication.backend.PlayerDedicationBackend;
import com.wezro.dedication.data.PlayerDedication;
import com.wezro.dedication.commands.DedicationSetTimerCommand;
import com.wezro.dedication.commands.DedicationTimerCommand;
import com.wezro.dedication.listeners.CitadelListener;
import com.wezro.dedication.listeners.LoginQuitListener;
import com.wezro.dedication.listeners.PvPListener;
import com.wezro.dedication.runnables.SafetyStorage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class Dedication extends JavaPlugin implements Listener {

    private static Dedication instance;

    private PlayerDedicationBackend backend;
    public static final String privilegeTokenKey = "DEDICATION_PRIVILEGE";
    public static final String playedTimeTokenKey = "SERVER_TIME";
    public static final String PREFIX = "[Dedication]";
    private Configuration configuration;

    @Override
    public void onEnable() {
        instance = this;
        //Listen for all the events in this class
        getServer().getPluginManager().registerEvents(this, this);
        //Save out configration if it doesnt exist.
        getConfig().options().copyDefaults(true);
        getConfig().options().copyHeader(true);
        saveConfig();

        configuration = new Configuration(this);
        switch (configuration.getBackendType()) {
            case FILE: {
                try {
                    backend = new FileBackend(this);
                } catch (IOException ex) {
                    throw new IllegalStateException("No backend.");
                }
            }
            break;
            default:
                throw new IllegalStateException("No backend.");
        }

        Bukkit.getPluginManager().registerEvents(new CitadelListener(), this);
        Bukkit.getPluginManager().registerEvents(new LoginQuitListener(), this);
        Bukkit.getPluginManager().registerEvents(new PvPListener(), this);

        Bukkit.getPluginCommand("dedicationtimer").setExecutor(new DedicationTimerCommand());
        Bukkit.getPluginCommand("dedicationsettimer").setExecutor(new DedicationSetTimerCommand());
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new SafetyStorage(), configuration.getSafetyStorage(), configuration.getSafetyStorage());
    }

    public static Dedication getInstance() {
        return instance;
    }

    public static boolean isPlayerDedicated(Player p) {
        return p.hasPermission("dedication.bypass") || hasDedicatedMetaTag(p)
                || (instance.backend.hasPlayer(p)
                && (instance.backend.getPlayerPlaytime(p) > instance.configuration.getHoursRequired()));
    }

    private static boolean hasDedicatedMetaTag(Player p) {
        for (MetadataValue val : p.getMetadata(privilegeTokenKey)) {
            if (val.getOwningPlugin() == instance) {
                return val.asBoolean();
            }
        }

        return false;
    }

    public static void addPlaytime(Player player, long l) {
        instance.backend.addPlaytime(player, l);
    }

    public static boolean hasPlayer(Player player) {
        return instance.backend.hasPlayer(player);
    }

    public static long getPlayerPlaytime(Player player) {
        return instance.backend.getPlayerPlaytime(player);
    }

    public static void addPlayer(Player player) {
        instance.backend.addPlayer(player);
    }

    public static Configuration getConfiguration() {
        return instance.configuration;
    }

    public static void forceSave() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (player.hasMetadata(playedTimeTokenKey)) {
                List<MetadataValue> timestamp = player.getMetadata(playedTimeTokenKey);
                for (MetadataValue value : timestamp) {
                    if (value.getOwningPlugin() == getInstance()) {
                        instance.backend.addPlaytime(player, System.currentTimeMillis() - value.asLong());
                        player.setMetadata(playedTimeTokenKey, new PlayerDedication(getInstance(), System.currentTimeMillis()));
                        break;
                    }
                }
            }
        }
    }

    //Simple little function to check how much time the player has left.
    public static long getTimePlayedInMillis(Player player) {
        if (player.hasMetadata(playedTimeTokenKey)) {

            long joinTime = player.getMetadata(playedTimeTokenKey).get(0).asLong();
            long now = System.currentTimeMillis();
            long hours = joinTime - now;
            long hoursPrev = instance.backend.getPlayerPlaytime(player);

            return hours + hoursPrev;
        } else {
            return 0;
        }
    }

    @Override
    public void onDisable() {
        forceSave();
    }
}
