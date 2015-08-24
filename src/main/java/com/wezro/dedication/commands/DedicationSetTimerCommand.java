/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wezro.dedication.commands;

import com.wezro.dedication.Dedication;
import static com.wezro.dedication.Dedication.playedTimeTokenKey;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author florian
 */
public class DedicationSetTimerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + cmd.getUsage() + "\nNot enough arguments.(" + args.length + " of 2)");
            return true;
        } else {
            String player = args[0];
            String time = args[1];
            int itime = 0;

            Player p = Bukkit.getPlayerExact(player);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + "Player not found. (" + player + ")");
            }

            try {
                itime = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Not a valid time value. (" + args[1] + ")");
                return true;
            }

            Dedication.addPlaytime(p, TimeUnit.HOURS.toMillis(itime));
            sender.sendMessage(ChatColor.GREEN + "Added " + args[1] + "h to " + args[0] + " play time.");
            sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Player " + args[0] + " played for "
                    + TimeUnit.MILLISECONDS.toHours(Dedication.getTimePlayedInMillis(p)) + "h ("
                    + TimeUnit.MILLISECONDS.toMinutes(Dedication.getTimePlayedInMillis(p)) + "m)");

            return true;
        }
    }
}
