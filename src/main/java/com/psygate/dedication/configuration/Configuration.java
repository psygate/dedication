package com.psygate.dedication.configuration;

import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.*;
import com.psygate.dedication.Helper;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Material;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class Configuration {

    public static enum BackendType {

        FILE
    };
    private long safetyStorageIntervalTicks;
    private BackendType backendType;
    private long strikeBackTime;
    private Set<Material> lockedBlocks = new HashSet<>();
    private Set<Target> targets = new HashSet();

    public Configuration() {
        final Dedication plugin = Dedication.getPlugin(Dedication.class);
        plugin.getConfig().options().copyDefaults(true);
        plugin.getConfig().options().copyHeader(true);
        plugin.saveConfig();

        this.safetyStorageIntervalTicks = plugin.getConfig().getLong("safetyStorage");
        this.backendType = BackendType.valueOf(plugin.getConfig().getString("backendType").toUpperCase());
        this.strikeBackTime = Helper.timeStringAsMillis(plugin.getConfig().getString("fallBackStrikeBackTime"));
        if (plugin.getServer().getPluginManager().getPlugin("CombatTagPlus") != null) {
            int duration = CombatTagPlus.getPlugin(CombatTagPlus.class).getSettings().getTagDuration();
            strikeBackTime = TimeUnit.SECONDS.toMillis(duration);
            Dedication.logger().log(Level.INFO, "Using CombatTagPlus timer. {0}s.", duration);
        } else {
            Dedication.logger().log(Level.INFO, "Using fall back timer. {0}ms.", strikeBackTime);
        }

        loadTargets(plugin);
        Dedication.logger().log(Level.INFO, "Loaded targets: {0}", targets);
        loadLockedBlocks(plugin);
        Dedication.logger().log(Level.INFO, "Loaded locked blocks: {0}", lockedBlocks);
    }

    private void loadTargets(Dedication plugin) {
        if (plugin.getConfig().contains("targets.minimumHoursPlayed")) {
            long target = Helper.timeStringAsMillis(plugin.getConfig().getString("targets.minimumHoursPlayed"));
            targets.add(new TimeTarget(0, target, null));
        }

        if (plugin.getConfig().contains("targets.blockbreaking")) {
            List<Map<String, Object>> sec = (List<Map<String, Object>>) plugin.getConfig().get("targets.blockbreaking");
            for (Map<String, Object> map : sec) {
                checkTargetMap(map);
                Material mat;
                boolean acceptAny;
                int amount;

                String type = (String) map.get("type");
                if ("*".equals(type)) {
                    mat = Material.AIR;
                    acceptAny = true;
                } else {
                    mat = Material.valueOf(type.toUpperCase());
                    acceptAny = false;
                }

                amount = (Integer) map.get("amount");
                targets.add(new BlockBreakTarget(mat, acceptAny, 0, amount, null));
            }
        }

        if (plugin.getConfig().contains("targets.blockplacing")) {
            List<Map<String, Object>> sec = (List<Map<String, Object>>) plugin.getConfig().get("targets.blockplacing");
            for (Map<String, Object> map : sec) {
                checkTargetMap(map);
                Material mat;
                int amount;
                boolean acceptAny;

                String type = (String) map.get("type");
                if ("*".equals(type)) {
                    mat = Material.AIR;
                    acceptAny = true;
                } else {
                    mat = Material.valueOf(type.toUpperCase());
                    acceptAny = false;
                }

                amount = (Integer) map.get("amount");
                targets.add(new BlockPlaceTarget(mat, acceptAny, 0, amount, null));
            }
        }

        if (plugin.getConfig().contains("targets.eating")) {
            List<Map<String, Object>> sec = (List<Map<String, Object>>) plugin.getConfig().get("targets.eating");
            for (Map<String, Object> map : sec) {
                checkTargetMap(map);
                Material mat;
                int amount;
                boolean acceptAny;

                String type = (String) map.get("type");
                if ("*".equals(type)) {
                    mat = Material.AIR;
                    acceptAny = true;
                } else {
                    mat = Material.valueOf(type.toUpperCase());
                    acceptAny = false;
                }

                amount = (Integer) map.get("amount");

                if (!acceptAny && !mat.isEdible()) {
                    throw new RuntimeException("Misconfiguration: Non-edible material: " + mat);
                }
                targets.add(new EdibleTarget(mat, acceptAny, 0, amount, null));
            }
        }
    }

    public PlayerData createPlayerData(UUID player) {
        PlayerData data = new PlayerData(player);
        for (Target tgt : targets) {
            Target cp = tgt.copy();
            cp.setUUID(player);
            data.addTarget(cp);
        }
        return data;
    }

    private void checkTargetMap(Map<String, Object> map) {
        if (!map.containsKey("type")) {
            throw new RuntimeException("Type declaration in targets missing.");
        }

        if (!map.containsKey("amount")) {
            throw new RuntimeException("Amount declaration in targets missing.");
        }
    }

    public long getSafetyStorageIntervalTicks() {
        return safetyStorageIntervalTicks;
    }

    public void setSafetyStorageIntervalTicks(long safetyStorageIntervalTicks) {
        this.safetyStorageIntervalTicks = safetyStorageIntervalTicks;
    }

    public BackendType getBackendType() {
        return backendType;
    }

    public void setBackendType(BackendType backendType) {
        this.backendType = backendType;
    }

    public long getStrikeBackTime() {
        return strikeBackTime;
    }

    public void setStrikeBackTime(long strikeBackTime) {
        this.strikeBackTime = strikeBackTime;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }

    public Set<Material> getLockedBlocks() {
        return lockedBlocks;
    }

    public void setLockedBlocks(Set<Material> lockedBlocks) {
        this.lockedBlocks = lockedBlocks;
    }

    @Override
    public String toString() {
        return "Configuration{" + "safetyStorageIntervalTicks=" + safetyStorageIntervalTicks + ", backendType=" + backendType + ", fallBackStrikeBackTime=" + strikeBackTime + ", lockedBlocks=" + lockedBlocks + ", targets=" + targets + '}';
    }

    private void loadLockedBlocks(Dedication plugin) {
        for (String s : plugin.getConfig().getStringList("lockedBlocks")) {
            lockedBlocks.add(Material.valueOf(s.toUpperCase()));
        }
    }
}
