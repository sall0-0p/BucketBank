package com.bucketbank;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.bucketbank.database.AccountsDatabase;

public class App extends JavaPlugin {
    private static Logger logger;
    private AccountsDatabase accountsDatabase;

    @Override
    public void onEnable() {
        // Logger for logs
        logger = getLogger();

        // Config loading
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        String greeting = config.getString("greeting-message");
        logger.info(greeting);

        // Database initialisation
        try {

            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            accountsDatabase = new AccountsDatabase(getDataFolder().getAbsolutePath() + "/accounts.db");

        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Failed to load accounts Database");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        // Database tests:
        try {
            String accountId = accountsDatabase.createAccount("_lordBucket");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Final Log
        logger.info("Banking loaded!");
    }

    @Override
    public void onDisable() {
        // Close Database
        try {
            accountsDatabase.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
            
        
        // Final log
        logger.info("Banking unloaded");
    }
}
