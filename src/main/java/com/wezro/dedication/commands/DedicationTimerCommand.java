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
public class DedicationTimerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender.isOp() && sender instanceof Player) {
            if (args.length > 0) {
                for (String s : args) {
                    Player p = Bukkit.getPlayer(s);
                    if (p != null && Dedication.hasPlayer(p)) {
                        long playedmillis = Dedication.getTimePlayedInMillis(p);
                        sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + "Player " + s + " played for "
                                + TimeUnit.MILLISECONDS.toHours(playedmillis) + "h ("
                                + TimeUnit.MILLISECONDS.toMinutes(playedmillis) + "m)");
                    } else {
                        sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Player not found: " + s);
                    }
                }
            } else {
                //Send the player the time they played
                long playedmillis = Dedication.getTimePlayedInMillis((Player) sender);
                sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + ChatColor.RESET
                        + "You have played: " + TimeUnit.MILLISECONDS.toHours(playedmillis) + "h ("
                        + TimeUnit.MILLISECONDS.toMinutes(playedmillis) + "m)");
            }
        } else if (sender instanceof Player) {
            //Send the player the time they played
            long playedmillis = Dedication.getTimePlayedInMillis((Player) sender);
            sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + ChatColor.RESET
                    + "You have played: " + TimeUnit.MILLISECONDS.toHours(playedmillis) + "h");
        } else {
            sender.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + Dedication.PREFIX + ChatColor.RESET
                    + "Cannot be used from command line.");
        }
        return true;
    }

}
