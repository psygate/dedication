/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication;

import com.wezro.dedication.backend.BackendType;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Material;

/**
 *
 * @author florian
 */
public class Configuration {

    private final long hoursRequired;
    private final long fallBackStrikeBackTime;
    private final BackendType backendType;
    private final Set<Material> lockedBlocks = new HashSet<>();
    private final long safetyStorage;

    public Configuration(Dedication plugin) {
        hoursRequired = Helper.timeStringAsMillis(plugin.getConfig().getString("minimumHoursPlayed"));
        fallBackStrikeBackTime = Helper.timeStringAsMillis(plugin.getConfig().getString("fallBackStrikeBackTime"));
        backendType = BackendType.valueOf(plugin.getConfig().getString("backendType").toUpperCase());

        List<String> locked = (List<String>) plugin.getConfig().getList("lockedBlocks");

        try {
            for (String lock : locked) {
                lockedBlocks.add(Material.getMaterial(lock.replace(" ", "_").toUpperCase()));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Dedication error. Loaded default locks.", e);

            lockedBlocks.add(Material.CHEST);
            lockedBlocks.add(Material.ENDER_CHEST);
            lockedBlocks.add(Material.TRAPPED_CHEST);
            lockedBlocks.add(Material.HOPPER);
            lockedBlocks.add(Material.FURNACE);
            lockedBlocks.add(Material.DROPPER);
            lockedBlocks.add(Material.DISPENSER);
        }

        safetyStorage = plugin.getConfig().getLong("safetyStorage");
    }

    public long getHoursRequired() {
        return hoursRequired;
    }

    public long getFallBackStrikeBackTime() {
        return fallBackStrikeBackTime;
    }

    public BackendType getBackendType() {
        return backendType;
    }

    public Set<Material> getLockedBlocks() {
        return Collections.unmodifiableSet(lockedBlocks);
    }

    public long getSafetyStorage() {
        return safetyStorage;
    }

}
