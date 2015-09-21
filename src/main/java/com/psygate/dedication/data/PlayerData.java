package com.psygate.dedication.data;

import com.psygate.dedication.Dedication;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class PlayerData implements Serializable {

    private UUID player;
    private Set<Target> targets = new HashSet();
    private Date timestamp = new Date(System.currentTimeMillis());
    private boolean adminOverride = false;

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

    public boolean isDedicated() {
        if (adminOverride) {
//            Dedication.logger().info("Satisfied by admin override.");

            return true;
        }

        Player pl = Dedication.getPlugin(Dedication.class).getServer().getPlayer(player);

        if (pl.hasPermission("dedication.bypass")) {
            return true;
        }

        for (Target tgt : targets) {
//            Dedication.logger().info("Target: " + tgt + " Satisfied: " + tgt.isSatisfied());
            if (!tgt.isSatisfied()) {
                return false;
            } else {
                System.out.println("Target satisfied. " + tgt);
            }
        }

        return true;
    }

    public boolean isAdminOverride() {
        return adminOverride;
    }

    public void setAdminOverride(boolean adminOverride) {
        this.adminOverride = adminOverride;
    }

    @Override
    public String toString() {
        return "PlayerData{" + "player=" + player + ", targets=" + targets
                + ", timestamp=" + timestamp + ", adminOverride="
                + adminOverride + ", has_override_permission="
                + Dedication.getPlugin(Dedication.class)
                .getServer().getPlayer(player).hasPermission("dedication.bypass") + "}";
    }

}
