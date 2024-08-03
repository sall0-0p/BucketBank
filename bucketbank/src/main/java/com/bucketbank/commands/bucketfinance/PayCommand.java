package com.bucketbank.commands.bucketfinance;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.Notification;
import com.bucketbank.modules.main.User;
import com.bucketbank.modules.managers.TransactionManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class PayCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private TransactionManager transactionManager = plugin.getTransactionManager();

    private Map<String, String> placeholders = new HashMap<>();

    // # %sender% - eitherid of account or username (depending on command) of person who sent
    // # %receiver% - either id of account or username (depending on command) of person who sent
    // # %amount% - amount of money sent
    // # %description% - description

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            String messageType;
            String receiverPlayerId;

            if (args.length > 2 && isPositiveInteger(args[2])) {
                if (isValidAccountId(args[0]) && isValidAccountId(args[1])) {
                    // bf pay accountId accountId amount reason | messageType: account_account
                    transactionManager.createTransaction(args[0], args[1], Integer.valueOf(args[2]), concatenateArgs(args, 3));
                    messageType = "account_account";
                    receiverPlayerId = new Account(args[1]).getOwnerId();

                    placeholders.put("%sender%", args[0]);
                    placeholders.put("%receiver%", args[1]);
                } else if (isValidAccountId(args[0]) && !isValidAccountId(args[1])) {
                    // bf pay accountId username amount reason | messageType: account_username
                    User destinationUser = new User(Bukkit.getOfflinePlayer(args[1]));

                    transactionManager.createTransaction(args[0], destinationUser.getPersonalAccountId(), Integer.valueOf(args[2]), concatenateArgs(args, 3));
                    messageType = "account_username";
                    receiverPlayerId = destinationUser.getUserId();

                    placeholders.put("%sender%", args[0]);
                    placeholders.put("%receiver%", destinationUser.getUsername());
                } else {
                    throw new Exception("Not proper arguments given!");
                }

                placeholders.put("%amount%", args[2]);
                placeholders.put("%description%", concatenateArgs(args, 3));
            } else if (isPositiveInteger(args[1])) {
                if (isValidAccountId(args[0])) {
                    // bf pay accountId amount reason | messageType: account
                    User senderUser = new User(((Player) sender).getUniqueId().toString());

                    transactionManager.createTransaction(senderUser.getPersonalAccountId(), args[0], Integer.valueOf(args[1]), concatenateArgs(args, 2));
                    messageType = "account";
                    receiverPlayerId = new Account(args[0]).getOwnerId();

                    placeholders.put("%sender%", senderUser.getUsername());
                    placeholders.put("%receiver%", args[0]);
                } else {
                    // bf pay username amount reason | messageType: username
                    User senderUser = new User(((Player) sender).getUniqueId().toString());
                    User destinationUser = new User(Bukkit.getOfflinePlayer(args[0]));
                    
                    transactionManager.createTransaction(senderUser.getPersonalAccountId(), destinationUser.getPersonalAccountId(), Integer.valueOf(args[1]), concatenateArgs(args, 2));
                    messageType = "username";
                    receiverPlayerId = destinationUser.getUserId();

                    placeholders.put("%sender%", senderUser.getUsername());
                    placeholders.put("%receiver%", destinationUser.getUsername());
                }

                placeholders.put("%amount%", args[1]);
                placeholders.put("%description%", concatenateArgs(args, 2));
            } else {
                throw new Exception("Amount has to be properly specified!");
            }

            // Setup placeholders

            // Print message
            String initialMessage = Messages.getString("pay." + messageType);
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);

            // Send message to receiver

            initialMessage = Messages.getString("pay_received." + messageType);
            parsedMessage = parsePlaceholders(initialMessage, placeholders);
            
            new Notification(receiverPlayerId, parsedMessage, true);
        } catch (Exception e) {
            Component component = mm.deserialize(Messages.getString("command_failed") + "<newline>| " + e.getMessage());
            sender.sendMessage(component);
            e.printStackTrace();
        }
    }

    private static String parsePlaceholders(String input, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            input = input.replace(entry.getKey(), entry.getValue());
        }
        return input;
    }

    private String concatenateArgs(String[] args, int number) {
        if (args.length >= number) {
            StringBuilder result = new StringBuilder();
            for (int i = number; i < args.length; i++) {
                if (i > number) {
                    result.append(" ");
                }
                result.append(args[i]);
            }
            return result.toString();
        } else {
            return Messages.getString("pay.default_note");
        }
    }

    private boolean isValidAccountId(String accountId) {
        if (accountId == null || accountId.length() != 6) {
            return false;
        }
        for (char c : accountId.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPositiveInteger(String str) {
        if (str == null) {
            return false;
        }
        try {
            int number = Integer.parseInt(str);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
