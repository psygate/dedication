/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication.listeners;

import com.wezro.dedication.Dedication;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 *
 * @author florian
 */
public class PvPListener implements Listener {

    private final CombatTagPlus ctp = (CombatTagPlus) Bukkit.getPluginManager().getPlugin("CombatTagPlus");
    private final long timeout = TimeUnit.SECONDS.toMillis(ctp.getConfig().getInt("tag-duration"));
    private final Map<UUID, AttackerRecord> attackers = new HashMap<>();

    public PvPListener() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(Dedication.getInstance(), new Cleaner(), 20 * 15, 20 * 15);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player attacker = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();
            boolean attackerdicated = Dedication.isPlayerDedicated(attacker);
            boolean victimdedicated = Dedication.isPlayerDedicated(victim);

            if (attackerdicated && !victimdedicated) {
                if (!attackers.containsKey(victim.getUniqueId())) {
                    AttackerRecord record = new AttackerRecord();
                    attackers.put(victim.getUniqueId(), record);
                }

                attackers.get(victim.getUniqueId()).addAttacker(attacker.getUniqueId());
                victim.sendMessage(Dedication.PREFIX + ChatColor.RED + "You are free to engage " + attacker.getName());
            } else if (!attackerdicated && victimdedicated) {
                subProcessEvent(event, victim, attacker);
            } else if (!attackerdicated && !victimdedicated) {
                attacker.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + "You cannot engage " + victim.getName() + ".");
                event.setCancelled(true);
            }
        }
    }

    private void subProcessEvent(EntityDamageByEntityEvent event, Player victim, Player attacker) {
        if (attackers.containsKey(attacker.getUniqueId())) {
            AttackerRecord rec = attackers.get(attacker.getUniqueId());
            if (!rec.canStrike(victim.getUniqueId())) {
                event.setCancelled(true);
                attacker.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + "You cannot engage " + victim.getName() + ".");

            }
        } else {
            attacker.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + "You cannot engage " + victim.getName() + ".");
            event.setCancelled(true);
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
