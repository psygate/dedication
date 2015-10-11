package com.psygate.dedication;

import com.psygate.dedication.backend.Backend;
import com.psygate.dedication.backend.FileBackend;
import com.psygate.dedication.commands.DedicationFlushCommand;
import com.psygate.dedication.commands.DedicationIgnoreCommand;
import com.psygate.dedication.commands.DedicationReloadCommand;
import com.psygate.dedication.commands.DedicationSetBypassCommand;
import com.psygate.dedication.commands.DedicationTimerCommand;
import com.psygate.dedication.configuration.Configuration;
import com.psygate.dedication.data.BlockBreakTarget;
import com.psygate.dedication.data.BlockPlaceTarget;
import com.psygate.dedication.data.EdibleTarget;
import com.psygate.dedication.listeners.BlockBreakListener;
import com.psygate.dedication.listeners.BlockPlaceListener;
import com.psygate.dedication.listeners.EdibleListener;
import com.psygate.dedication.listeners.LoginLogoutListener;
import com.psygate.dedication.data.PlayerData;
import com.psygate.dedication.data.Target;
import com.psygate.dedication.data.TimeTarget;
import com.psygate.dedication.listeners.CitadelListener;
import com.psygate.dedication.listeners.PvPListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class Dedication extends JavaPlugin {

    public static final String PREFIX = "[Dedication]";
    private static Dedication instance = null;
    private final static boolean DEBUG = false;

    private Configuration configuration;
    private BlockBreakListener bbl;
    private BlockPlaceListener bpl;
    private EdibleListener el;
    private LoginLogoutListener lll;
    private PvPListener pvp;
    private Backend backend;
    private final Map<UUID, PlayerData> playerdata = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        reloadConfiguration();
        if (DEBUG == true) {
            debug();
        }
        switch (configuration.getBackendType()) {
            case FILE:
                backend = new FileBackend(new File(getDataFolder(), "dedication_data"));
        }

        logger().log(Level.INFO, "Configuration: {0}", configuration);

        bbl = new BlockBreakListener();
        bpl = new BlockPlaceListener();
        el = new EdibleListener();
        lll = new LoginLogoutListener();
        pvp = new PvPListener();

        registerListener(bbl, bpl, el, lll, pvp);

        if (getServer()
                .getPluginManager().getPlugin("Citadel") != null) {
            logger().info("Citadel found. Adding citadel listener.");
            registerListener(new CitadelListener(playerdata, pvp));
        } else {
            logger().info("Citadel not found. Skipping citadel listener.");
        }

        reloadCache();

        registerCommands();
    }

    @Override
    public void onDisable() {
        for (UUID uuid : playerdata.keySet()) {
            savePlayer(uuid);
        }
    }

    public static void flushCache() {
        ArrayList<UUID> cpy = new ArrayList<>(instance.playerdata.keySet());
        for (UUID uuid : cpy) {
            saveAndRemovePlayer(uuid);
        }
    }

    public static void reloadCache() {
        instance.playerdata.clear();
        ArrayList<Player> cpy = new ArrayList<>(instance.getServer().getOnlinePlayers());
        for (Player p : cpy) {
            initPlayer(p.getUniqueId());
        }
    }

    public static void reloadConfiguration() {
        instance.configuration = new Configuration();
    }

    public static Configuration getConfiguration() {
        if (instance.configuration == null) {
            reloadConfiguration();
        }
        return instance.configuration;
    }

    private void registerListener(Listener... li) {
        for (Listener l : li) {
            getServer().getPluginManager().registerEvents(l, this);
        }
    }

    public static Logger logger() {
        return instance.getLogger();
    }

    private void debug() {
        new File(getDataFolder(), "config.yml").delete();
        getLogger().setLevel(Level.FINER);
    }

    public static PlayerData initPlayer(UUID player) {
        if (instance.playerdata.containsKey(player)) {
            return instance.playerdata.get(player);
        }
        PlayerData data;
        if (instance.backend.hasPlayer(player)) {
            logger().log(Level.FINE, "Loaded data from backend for {0}", player);
            data = instance.backend.loadPlayerData(player);
        } else {
            logger().log(Level.FINE, "Created data from backend for {0}", player);
            data = instance.configuration.createPlayerData(player);
        }

        for (Target t : data.getTargets()) {
            switch (t.getType()) {
                case BLOCK_BREAK:
                    instance.bbl.addTarget((BlockBreakTarget) t);
                    break;
                case BLOCK_PLACE:
                    instance.bpl.addTarget((BlockPlaceTarget) t);
                    break;
                case CONSUMPTION:
                    instance.el.addTarget((EdibleTarget) t);
                    break;
                case TIME:
                    instance.lll.addTarget((TimeTarget) t);
                    break;
                default:
                    logger().log(Level.WARNING, "Unkown target type: {0}", t.getType());
            }
        }
        if (data.isAdminOverride()) {
            logger().log(Level.INFO, "{0} is admin cleared from dedication.", player);
        }
        instance.playerdata.put(player, data);
        return data;
    }

    public static void setIgnore(UUID player, long minutes) {
        initPlayer(player).setIgnoringUntil(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(minutes));
    }

    public static void sendMessage(UUID player, String message) {
        Player p = instance.getServer().getPlayer(player);
        if (p != null) {
            sendMessage(p, message);
        }
    }

    public static void sendMessage(Player player, String message) {
        if (initPlayer(player.getUniqueId()).getIgnoringUntil() <= System.currentTimeMillis()) {
            player.sendMessage(message);
        }
    }

    public static void saveAndRemovePlayer(UUID player) {
        PlayerData data = instance.playerdata.remove(player);

        if (data == null) {
            logger().log(Level.WARNING, "Odd... {0} was never loaded...", player);
        } else {
            instance.backend.savePlayerData(data);
        }
    }

    public static void savePlayer(UUID player) {
        PlayerData data = instance.playerdata.get(player);

        if (data == null) {
            logger().log(Level.WARNING, "Odd... {0} was never loaded...", player);
        } else {
            instance.backend.savePlayerData(data);
        }
    }

    private void registerCommands() {
        getServer().getPluginCommand("timer").setExecutor(new DedicationTimerCommand());
        getServer().getPluginCommand("flush").setExecutor(new DedicationFlushCommand());
        getServer().getPluginCommand("dreload").setExecutor(new DedicationReloadCommand());
        getServer().getPluginCommand("bypass").setExecutor(new DedicationSetBypassCommand());
        getServer().getPluginCommand("mute").setExecutor(new DedicationIgnoreCommand());
    }

    public static List<PlayerData> loadAllByName(String name) {
        return instance.backend.getAllByName(name);
    }
}
