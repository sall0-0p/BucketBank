package com.bucketbank;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.bucketbank.modules.CommandHandler;
import com.bucketbank.modules.DatabaseManager;
import com.bucketbank.modules.Tests;

public class App extends JavaPlugin {
    private static Logger logger;
    private static DatabaseManager databaseManager;
    private static App plugin;
    
    @Override
    public void onEnable() {
        // Logger for logs
        logger = getLogger();
        plugin = this;

        // Config loading
        saveDefaultConfig();

        // Init Databases
        databaseManager = new DatabaseManager();

        // Register commands
        this.getCommand("bucketfinance").setExecutor(new CommandHandler());

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
}
