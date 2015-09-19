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
public class DedicationTimerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if (strings.length > 0 && !cs.hasPermission("dedication.admin.seeothers")) {
            cs.sendMessage(Dedication.PREFIX + ChatColor.RED + "You cannot see the dedication of others. This incident will be logged.");
            cs.sendMessage(Dedication.PREFIX + ChatColor.RED + "To see your own dedication, issue this command without arguments.");
            return true;
        } else {
            if (cs.hasPermission("dedication.admin.extensiveinfo")) {
                cs.sendMessage(Dedication.PREFIX + "Info follows:");
                if (cs instanceof ConsoleCommandSender && strings.length == 0) {
                    cs.sendMessage(Dedication.PREFIX + ChatColor.RED + "Console doesn't have any dedication data. Append a user name.");
                } else {
                    extensiveInfo(cs, (strings.length == 0) ? new String[]{cs.getName()} : strings);
                }
            } else {
                info(cs);
            }
        }
        return true;
    }

    private void extensiveInfo(CommandSender cs, String[] strings) {
        for (String name : strings) {
            Player p = Dedication.getPlugin(Dedication.class).getServer().getPlayer(name);
            if (p != null) {
                PlayerData data = Dedication.initPlayer(p.getUniqueId());
                cs.sendMessage(Dedication.PREFIX + "Data for " + name + ":");
                cs.sendMessage(data.toString());
            } else {
                cs.sendMessage(Dedication.PREFIX + ChatColor.RED + "Player not found: " + name + " (is that player online?)");
            }
        }
    }

    private void info(CommandSender cs) {
        if (!(cs instanceof Player)) {
            Dedication.logger().severe("Non-players cannot invoke this method.");
        } else {
            Player p = (Player) cs;

            PlayerData data = Dedication.initPlayer(p.getUniqueId());

            long numericTarget = 0;
            long numericDone = 0;

            for (Target t : data.getTargets()) {
                if (t instanceof NumericTarget) {
                    NumericTarget num = (NumericTarget) t;
                    numericTarget += num.getTarget();
                    numericDone += num.getValue();
                } else {
                    Dedication.logger().log(Level.WARNING, "No rule for: {0}", t.getClass());
                }
            }

            cs.sendMessage(Dedication.PREFIX + "Progress: " + ((numericDone / numericTarget) * 100) + "%");
        }
    }
}
