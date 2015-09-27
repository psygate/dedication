package com.psygate.dedication.commands;

import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.NumericTarget;
import com.psygate.dedication.data.PlayerData;
import com.psygate.dedication.data.Target;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class DedicationSetBypassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (strings.length == 0 && !(cs instanceof Player)) {
            cs.sendMessage(Dedication.PREFIX + ChatColor.RED + "Console can't bypass.");
        } else {
            if (strings.length == 0) {
                Player p = (Player) cs;
                bypass(p);
            } else {
                for (String name : strings) {
                    Player bp = Dedication.getPlugin(Dedication.class).getServer().getPlayer(name);
                    if (bp == null) {
                        cs.sendMessage(Dedication.PREFIX + ChatColor.RED + "Cannot bypass offline players.");
                    } else {
                        bypass(bp);
                        if (Dedication.initPlayer(bp.getUniqueId()).isAdminOverride()) {
                            cs.sendMessage(Dedication.PREFIX + ChatColor.GREEN + name + " is now bypassing dedication.");

                        } else {
                            cs.sendMessage(Dedication.PREFIX + ChatColor.RED + name + " is not bypassing dedication anymore.");
                        }
                    }
                }
            }
        }
        return true;
    }

    private void bypass(Player p) {
        PlayerData data = Dedication.initPlayer(p.getUniqueId());

        if (!data.isAdminOverride()) {
            p.sendMessage(Dedication.PREFIX + ChatColor.GREEN + "You are now bypassing dedication.");
            data.setAdminOverride(true);
        } else {
            p.sendMessage(Dedication.PREFIX + ChatColor.RED + "You are not bypassing dedication anymore.");
            data.setAdminOverride(false);
        }
    }
}
