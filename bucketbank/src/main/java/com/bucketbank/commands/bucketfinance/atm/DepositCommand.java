package com.bucketbank.commands.bucketfinance.atm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.main.Account;
import com.bucketbank.modules.managers.ATMManager;
import com.bucketbank.modules.managers.CurrencyManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class DepositCommand implements Command {
    App plugin = App.getPlugin();
    CurrencyManager currencyManager = plugin.getCurrencyManager();
    ATMManager atmManager = App.getATMManager();
    private static final MiniMessage mm = MiniMessage.miniMessage();
    
    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                throw new Exception("Sender must be player!");
            }
    
            if (!sender.hasPermission("bucketfinance.atm")) {
                throw new Exception("You have no permission to use this command!");
            }
    
            if (!atmManager.isPlayerNearATM((Player) sender) && !sender.hasPermission("bucketfinance.atm.remote")) {
                throw new Exception("You need to be near ATM to use this command!");
            }
    
            boolean result = currencyManager.depositCurrency((Player) sender, Integer.valueOf(args[1]));
    
            Account account = new Account(args[0]);
            
            if (result) {
                account.modifyBalance(Integer.valueOf(args[1]));
            }
        } catch (Exception e) {
            Component component = mm.deserialize("<red>| " + e.getMessage());
            sender.sendMessage(component);
        }
    }
}
