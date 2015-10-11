package com.psygate.dedication.commands;

import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.MaterialTarget;
import com.psygate.dedication.data.NumericTarget;
import com.psygate.dedication.data.PlayerData;
import com.psygate.dedication.data.Target;
import com.psygate.dedication.data.TimeTarget;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.ChatColor;
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
                cs.sendMessage(toString(data));
            } else {
//                cs.sendMessage(Dedication.PREFIX + ChatColor.RED + "Player not found: " + name + " (is that player online?)");
                for (PlayerData data : Dedication.loadAllByName(name)) {
                    cs.sendMessage(toString(data));
                }
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

            cs.sendMessage("Status: " + ((data.isDedicated()) ? ChatColor.GREEN + "DEDICATED" : ChatColor.RED + "NOT DEDICATED."));
            if (numericTarget == 0) {
                cs.sendMessage(Dedication.PREFIX + "Progress: 0%");
            } else {
                int perc = (int) (((double) numericDone / (double) numericTarget) * 100);
                cs.sendMessage(Dedication.PREFIX + " Progress: " + ((perc > 100) ? 100 : perc) + "%");
            }
        }
    }

    private String[] toString(PlayerData playerdata) {
        if (playerdata == null) {
            return new String[]{"NULL"};
        }
        ArrayList<String> msgs = new ArrayList<>();
        msgs.add("PlayerData for " + playerdata.getPlayer() + " (" + playerdata.getPlayerNames() + "):");

        if (playerdata.getTargets().isEmpty()) {
            msgs.add(ChatColor.RED + "NO TARGETS FOR PLAYER. BROKEN DEDICATION DATA.");
        } else {

            for (Target t : playerdata.getTargets()) {
                msgs.add("Type: " + t.getType().name());

                if (t instanceof NumericTarget) {
                    NumericTarget nt = (NumericTarget) t;
                    double perc = (double) nt.getValue() / (double) nt.getTarget();
                    msgs.add(" Value: " + nt.getValue() + "/" + nt.getTarget() + " [" + ((int) Math.floor(perc * 100) + "%]"));
                }

                if (t instanceof MaterialTarget) {
                    MaterialTarget mt = (MaterialTarget) t;
                    if (!mt.isAcceptAny()) {
                        msgs.add("    Material: " + mt.getMaterial());
                    } else {
                        msgs.add("     Material: <ANY>");
                    }
                }

                if (t instanceof TimeTarget) {
                    TimeTarget tt = (TimeTarget) t;
                    msgs.add("     Hours: " + TimeUnit.MILLISECONDS.toHours(tt.getValue()) + "/" + TimeUnit.MILLISECONDS.toHours(tt.getTarget()));
                }

                if (t.isSatisfied()) {
                    msgs.add(ChatColor.GREEN + "[Satisified[" + ChatColor.RESET);
                } else {
                    msgs.add(ChatColor.DARK_RED + "[Not Satisfied.]" + ChatColor.RESET);
                }

            }

            if (playerdata.isDedicated()) {
                msgs.add(ChatColor.GREEN + "Player is dedicated (Including permissions and overrides)." + ChatColor.RESET);
            }
            if (playerdata.isDedicatedNoOverrideNoPermission()) {
                msgs.add(ChatColor.GREEN + "Player is normally dedicated (Not including permissions and overrides)." + ChatColor.RESET);
            }

            if (playerdata.isAdminOverride()) {
                msgs.add(ChatColor.RED + "Player is dedicated by admin override." + ChatColor.RESET);
            }
        }

//        if (playerdata.isAdminOverride()) {
//            out += "\n" + ChatColor.BLUE + "Player is force dedicated by admin override." + ChatColor.RESET;
//        } else if (!playerdata.isDedicatedNoOverrideNoPermission()) {
//            out += "\n" + ChatColor.RED + "Player is not dedicated." + ChatColor.RESET;
//        } else if (playerdata.isDedicatedNoOverrideNoPermission()) {
//            out += "\n" + ChatColor.GREEN + "Player is dedicated." + ChatColor.RESET;
//        }
        return msgs.toArray(new String[0]);
    }
}
