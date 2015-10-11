package com.psygate.dedication.data;

import com.psygate.dedication.Dedication;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class PlayerData implements Serializable {

    private UUID player;
    private Set<Target> targets = new HashSet();
    private Date timestamp = new Date(System.currentTimeMillis());
    private boolean adminOverride = false;
    private Set<String> playerNames = new HashSet<>();
    private long ignoringUntil = 0;

    public PlayerData() {
        //Bean constructor.
    }

    public PlayerData(UUID player) {
        this.player = player;
    }

    public void addTarget(Target tgt) {
        targets.add(tgt);
    }

    public UUID getPlayer() {
        return player;
    }

    public Set<Target> getTargets() {
        return targets;
    }

    public void setTargets(Set<Target> targets) {
        this.targets = targets;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public long getIgnoringUntil() {
        return ignoringUntil;
    }

    public void setIgnoringUntil(long ignoringUntil) {
        this.ignoringUntil = ignoringUntil;
    }

    public boolean isDedicatedNoOverrideNoPermission() {
        for (Target tgt : targets) {
//            Dedication.logger().info("Target: " + tgt + " Satisfied: " + tgt.isSatisfied());
            if (!tgt.isSatisfied()) {
                return false;
            }
        }

        return true;
    }

    public boolean isDedicated() {
        if (adminOverride) {
//            Dedication.logger().info("Satisfied by admin override.");

            return true;
        }

        Player pl = Dedication.getPlugin(Dedication.class).getServer().getPlayer(player);

        if (pl.hasPermission("dedication.bypass")) {
            return true;
        }

        return isDedicatedNoOverrideNoPermission();

    }

    public boolean isAdminOverride() {
        return adminOverride;
    }

    public void setAdminOverride(boolean adminOverride) {
        this.adminOverride = adminOverride;
    }

    public Set<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(Set<String> playerNames) {
        this.playerNames = playerNames;
    }

    @Override
    public String toString() {
        return "PlayerData{" + "player=" + player + ", targets=" + targets + ", timestamp=" + timestamp + ", adminOverride=" + adminOverride + ", playerNames=" + playerNames + '}';
    }

}
