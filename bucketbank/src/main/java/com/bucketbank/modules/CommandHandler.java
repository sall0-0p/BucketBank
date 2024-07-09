package com.bucketbank.modules;

import java.util.HashMap;
import java.util.Map;

// import org.bukkit.command.Command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bucketbank.commands.bucketfinance.AboutCommand;
import com.bucketbank.commands.bucketfinance.user.AboutUserCommand;

public class CommandHandler implements CommandExecutor {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandHandler() {
        commands.put("about", new AboutCommand());
        commands.put("user about", new AboutUserCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /bucketfinance <command> [sub-command] [arguments]");
            return true;
        }

        StringBuilder subCommandPath = new StringBuilder(args[0]);
        Command command = commands.get(subCommandPath.toString());

        int index = 1;
        while (command == null && index < args.length) {
            subCommandPath.append(" ").append(args[index]);
            command = commands.get(subCommandPath.toString());
            index++;
        }

        if (command == null) {
            sender.sendMessage("Unknown command. Type \"/help\" for help.");
            return true;
        }

        // Create a new array for the remaining arguments
        String[] subCommandArgs = new String[args.length - index];
        System.arraycopy(args, index, subCommandArgs, 0, args.length - index);

        command.execute(sender, subCommandArgs);

        return true;
    }
}
