package com.bucketbank;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.bucketbank.modules.CommandCompleter;
import com.bucketbank.modules.CommandHandler;
import com.bucketbank.modules.Messages;
import com.bucketbank.modules.Tests;
import com.bucketbank.modules.managers.CreditManager;
import com.bucketbank.modules.managers.CurrencyManager;
import com.bucketbank.modules.managers.DatabaseManager;
import com.bucketbank.modules.managers.NotificationManager;
import com.bucketbank.modules.managers.TransactionManager;

public class App extends JavaPlugin {
    private static Logger logger;
    private static DatabaseManager databaseManager;
    private static TransactionManager transactionManager;
    private static App plugin;
    private static CurrencyManager currencyManager;

    @Override
    public void onEnable() {
        // Logger for logs
        logger = getLogger();
        plugin = this;

        // Config loading
        saveDefaultConfig();
        new Messages();
        currencyManager = new CurrencyManager();

        // Init Databases
        databaseManager = new DatabaseManager();
        transactionManager = new TransactionManager();

        // Init credits
        new CreditManager();

        // Register commands
        this.getCommand("bucketfinance").setExecutor(new CommandHandler());
        this.getCommand("bucketfinance").setTabCompleter(new CommandCompleter());
        getServer().getPluginManager().registerEvents(new NotificationManager(), this);

        // Tests
        new Tests();

        // Final Log
        logger.info("Banking loaded!");
    }

    @Override
    public void onDisable() {
        databaseManager.closeConnections();

        // Final log
        logger.info("Banking unloaded");
    }

    public static App getPlugin() {
        return plugin;
    }

    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public static TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public static CurrencyManager getCurrencyManager() {
        return currencyManager;
    }
}
