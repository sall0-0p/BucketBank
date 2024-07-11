package com.bucketbank.commands.bucketfinance.account.balance;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.main.User;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class SetBalanceCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    private Map<String, String> placeholders = new HashMap<>();

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (args.length != 2) {
                throw new Exception("accountId and value have to be provided!");
            }

            Account account;
            String messageType;
            if (isValidAccountId(args[0])) {
                if (Account.exists(args[0])) {
                    messageType = "balance_account";
                    account = new Account(args[0]);

                    try {
                        account.setBalance(Integer.parseInt(args[1]));
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new Exception("Invalid number or failed to set balance");
                    }
                } else {
                    throw new Exception("Account does not exist!");
                }
            } else {
                throw new Exception("This is not account id!");
            }

            // Setup placeholders
            placeholders.put("%owner%", new User(account.getOwnerId()).getUsername());
            placeholders.put("%ownerId%", account.getAccountId());
            placeholders.put("%accountId%", account.getAccountId());
            placeholders.put("%balance%", String.valueOf(account.getBalance()));

            // Print message
            String initialMessage = Messages.getString("account.set_balance");
            String parsedMessage = parsePlaceholders(initialMessage, placeholders);

            Component component = mm.deserialize(parsedMessage);
            sender.sendMessage(component);
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
}
