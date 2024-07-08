package com.bucketbank;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import com.bucketbank.modules.autoupdates.UpdateDownloader;

public class App extends JavaPlugin {
    private static Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();

        logger.info("Banking loading!");
        logger.info("Checking for updates!");

        UpdateDownloader.checkForUpdates(this.getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("See you again, SpigotMC!");
    }
}
