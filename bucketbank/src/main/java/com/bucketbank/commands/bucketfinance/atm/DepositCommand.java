package com.bucketbank.commands.bucketfinance.atm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.managers.CurrencyManager;

public class DepositCommand implements Command {
    App plugin = App.getPlugin();
    CurrencyManager currencyManager = plugin.getCurrencyManager();
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean result = currencyManager.depositCurrency((Player) sender, Integer.valueOf(args[1]));

        Account account = new Account(args[0]);
        
        if (result) {
            try {
                account.modifyBalance(Integer.valueOf(args[1]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
