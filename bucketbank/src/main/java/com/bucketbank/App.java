package com.bucketbank;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.bucketbank.modules.DatabaseManager;

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
