package com.psygate.dedication.commands;

import com.psygate.dedication.Dedication;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class DedicationReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        Dedication.reloadCache();
        Dedication.logger().log(Level.WARNING, "CommandSender: {0} issued a reload command.", cs);
        cs.sendMessage("Reloaded data.");
        return true;
    }

}
