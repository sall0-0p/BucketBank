package com.bucketbank.commands.bucketfinance.atm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.managers.CurrencyManager;

public class WithdrawCommand implements Command {
    App plugin = App.getPlugin();
    CurrencyManager currencyManager = plugin.getCurrencyManager();

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (args.length > 1) {
                Account account = new Account(args[0]);
                int amount = Integer.valueOf(args[1]);

                if (account.getBalance() > amount) {
                    boolean result = currencyManager.withdrawCurrency(player, amount);
                
                    if (result) {
                        try {
                            account.modifyBalance(-amount);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
