package com.psygate.dedication.listeners;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.PlayerData;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import net.minecraft.server.v1_8_R3.EntityArrow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;

/**
 *
 * @author florian
 */
public class PvPListener implements Listener {

    private final long timeout;
    private final Map<UUID, AttackerRecord> attackers = new HashMap<>();

    public PvPListener() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Dedication.getPlugin(Dedication.class), new Cleaner(), 20 * 15, 20 * 15);
        timeout = Dedication.getConfiguration().getStrikeBackTime();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            boolean attackerdicated = Dedication.initPlayer(attacker.getUniqueId()).isDedicated();
            boolean victimdedicated = Dedication.initPlayer(victim.getUniqueId()).isDedicated();

            process(event, attacker, victim, attackerdicated, victimdedicated);
        } else if (event.getDamager() instanceof Projectile && event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Entity check = event.getDamager();
            if (check.hasMetadata("dedication")) {
                for (MetadataValue v : event.getDamager().getMetadata("dedication")) {
                    if (v instanceof DedicationMeta) {
                        DedicationMeta meta = (DedicationMeta) v;
                        Player attacker = Dedication.getPlugin(Dedication.class).getServer().getPlayer((UUID) meta.value());

                        boolean attackerdicated = meta.asBoolean();
                        boolean victimdedicated = Dedication.initPlayer(victim.getUniqueId()).isDedicated();

                        process(event, attacker, victim, attackerdicated, victimdedicated);
                        return;
                    }

                }
            }

        }
    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
//    public void onArrow(ProjectileHitEvent ev) {
//        if (ev.getEntityType() == EntityType.PLAYER) {
//            Player p = (Player) ev.getEntity();
//            if (Dedication.initPlayer(p.getUniqueId()).isDedicated()) {
//                ev.getProjectile().setMetadata("dedication", new ProjectileMeta(p.getUniqueId(), true));
//            } else {
//                ev.getProjectile().setMetadata("dedication", new ProjectileMeta(p.getUniqueId(), false));
//            }
//        }
//    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntitySpawn(ProjectileLaunchEvent ev) {
        ProjectileSource source = ev.getEntity().getShooter();
        Dedication.logger().log(Level.INFO, "Spawned {0} by {1}", new Object[]{ev.getEntityType(), source});

        if (source instanceof Player) {
            Player player = (Player) source;
            PlayerData data = Dedication.initPlayer(player.getUniqueId());
            ev.getEntity().setMetadata("dedication", new DedicationMeta(player.getUniqueId(), data.isDedicated()));
            Dedication.logger().log(Level.INFO, "Attached meta. {0} by {1}", new Object[]{ev.getEntityType(), source});

        }
    }

    private void subProcessEvent(EntityDamageByEntityEvent event, Player victim, Player attacker) {
        if (attackers.containsKey(attacker.getUniqueId())) {
            AttackerRecord rec = attackers.get(attacker.getUniqueId());
            if (!rec.canStrike(victim.getUniqueId())) {
                event.setCancelled(true);
                noEngageMsg(attacker, victim);
            }
        } else {
            noEngageMsg(attacker, victim);
            event.setCancelled(true);
        }
    }

    private void noEngageMsg(Player attacker, Player victim) {
        attacker.sendMessage(Dedication.PREFIX + ChatColor.RED + "You cannot engage " + victim.getName() + ".");
    }

    private void engageMsg(Player attacker, Player victim) {
        victim.sendMessage(Dedication.PREFIX + ChatColor.RED + " You are free to engage " + attacker.getName());
    }

    private void process(EntityDamageByEntityEvent event, Player attacker, Player victim, boolean attackerdicated, boolean victimdedicated) {
        if (attackerdicated && !victimdedicated) {
            if (!attackers.containsKey(victim.getUniqueId())) {
                AttackerRecord record = new AttackerRecord();
                attackers.put(victim.getUniqueId(), record);
            }

            attackers.get(victim.getUniqueId()).addAttacker(attacker.getUniqueId());
            engageMsg(attacker, victim);
        } else if (!attackerdicated && victimdedicated) {
            subProcessEvent(event, victim, attacker);
        } else if (!attackerdicated && !victimdedicated) {
            noEngageMsg(attacker, victim);
            event.setCancelled(true);
        }
    }

//    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
//    public void blockChange(BlockIgniteEvent ev) {
//        if (ev.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL
//                && ev.getIgnitingEntity() != null
//                && ev.getIgnitingEntity() instanceof Player) {
//            Player igniter = (Player) ev.getIgnitingEntity();
//            PlayerData data = Dedication.initPlayer(igniter.getUniqueId());
//            ev.getBlock().setMetadata("dedication", new DedicationMeta(igniter.getUniqueId(), data.isDedicated()));
//        }
//    }
    private class DedicationMeta implements MetadataValue {

        private final UUID shotBy;
        private final boolean dedicated;
        private boolean valid = true;

        public DedicationMeta(UUID shotBy, boolean dedicated) {
            this.shotBy = shotBy;
            this.dedicated = dedicated;
        }

        @Override
        public Object value() {
            checkValid();
            return shotBy;
        }

        @Override
        public int asInt() {
            checkValid();
            return (dedicated) ? 1 : 0;
        }

        @Override
        public float asFloat() {
            checkValid();

            return (dedicated) ? 1 : 0;
        }

        @Override
        public double asDouble() {
            checkValid();

            return (dedicated) ? 1 : 0;
        }

        @Override
        public long asLong() {
            checkValid();

            return (dedicated) ? 1 : 0;
        }

        @Override
        public short asShort() {
            checkValid();

            return (short) ((dedicated) ? 1 : 0);
        }

        @Override
        public byte asByte() {
            checkValid();

            return (byte) ((dedicated) ? 1 : 0);
        }

        @Override
        public boolean asBoolean() {
            checkValid();
            return dedicated;
        }

        @Override
        public String asString() {
            checkValid();
            return Boolean.toString(dedicated);
        }

        private void checkValid() {
            if (!valid) {
                throw new IllegalStateException("Meta data is invalid");
            }
        }

        @Override
        public Plugin getOwningPlugin() {
            return Dedication.getPlugin(Dedication.class);
        }

        @Override
        public void invalidate() {
            valid = false;
        }

    }

    private class AttackerRecord {

        private final Map<UUID, Long> attackers;

        public AttackerRecord() {
            attackers = new HashMap<>();
        }

        public Map<UUID, Long> getAttackers() {
            return attackers;
        }

        public void addAttacker(UUID attacker) {
            attackers.put(attacker, System.currentTimeMillis());
        }

        public boolean canStrike(UUID victim) {
            return System.currentTimeMillis() - attackers.getOrDefault(victim, 0L) <= timeout;
        }

        public boolean isEmpty() {
            return attackers.isEmpty();
        }
    }

    private class Cleaner implements Runnable {

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, AttackerRecord>> it = attackers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, AttackerRecord> en = it.next();
                Iterator<Map.Entry<UUID, Long>> it2 = en.getValue().getAttackers().entrySet().iterator();

                while (it2.hasNext()) {
                    Map.Entry<UUID, Long> en2 = it2.next();

                    if (time - en2.getValue() > timeout) {
                        it2.remove();
                    }
                }

                if (en.getValue().isEmpty()) {
                    it.remove();
                }
            }
        }
    }
}
