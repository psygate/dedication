package com.psygate.dedication.commands;

import com.psygate.dedication.Dedication;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class DedicationIgnoreCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if(arg3.length > 0){
            if (arg0 instanceof Player) {
                long minutes = 0;
                try {
                    minutes = Long.parseLong(arg3[0]);
                } catch (NumberFormatException e) {
                    arg0.sendMessage(ChatColor.RED + "Malformed argument.");
                    return true;
                }
                Dedication.setIgnore(((Player) arg0).getUniqueId(), minutes);
                arg0.sendMessage("Dedication notifications ignored for " + minutes + " minutes.");
                arg0.sendMessage("To reset this, issue /dedication:mute 0 or /di 0");
            } else {
                arg0.sendMessage(ChatColor.RED + "Only players can do that.");
            }
        } else {
            arg0.sendMessage(ChatColor.RED + "Please put an argument after /dedication:mute TIME or /di TIME");
        }
        return true;
    }
}