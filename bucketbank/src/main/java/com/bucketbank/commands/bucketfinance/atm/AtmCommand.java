package com.bucketbank.commands.bucketfinance.atm;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.database.UsersDatabase;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AtmCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private static final UsersDatabase usersDatabase = (plugin.getDatabaseManager()).getUsersDatabase();
    String initialMessage;

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        String senderUUID = ((Player) sender).getUniqueId().toString();
        try {
            if (args.length > 0) {
                initialMessage = Messages.getString("atm.main_page");
                placeholders.put("%accountId%", args[0]);
                
            } else {
                if (usersDatabase.userExists(senderUUID)) {
                    initialMessage = Messages.getString("atm.main_page");
                    placeholders.put("%accountId%", new User(senderUUID).getPersonalAccountId());
                } else {
                    initialMessage = Messages.getString("atm.exchange_page");
                }
            }

            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
        } catch (Exception e) {

        }
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }
}
