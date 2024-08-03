package com.bucketbank.modules.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.bucketbank.App;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CurrencyManager {
    private static final MiniMessage mm = MiniMessage.miniMessage();
    App plugin = App.getPlugin();
    Logger logger = plugin.getLogger();


    public CurrencyManager() {}

    @SuppressWarnings("deprecation")
    public boolean validateCurrency(ItemStack item) {
        FileConfiguration config = plugin.getConfig();

        int CURRENCY_MODEL_DATA = config.getInt("currency.customModelData");
        String CURRENCY_NAME = config.getString("currency.name");
        String CURRENCY_MATERIAL = config.getString("currency.item");
    
        if (item != null && item.getType() == Material.getMaterial(CURRENCY_MATERIAL)) {
            // Check for specific name and lore
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                if (ChatColor.stripColor(meta.getDisplayName()).equals(CURRENCY_NAME)) {
                    return true;
                }
            }

            // Check for specific CustomModelData
            if (meta != null && meta.hasCustomModelData()) {
                if (meta.getCustomModelData() == CURRENCY_MODEL_DATA) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean depositCurrency(Player player, int amount) {
        List<ItemStack> itemsToRemove = new ArrayList<>();
        int count = 0;
    
        logger.info("Starting depositCurrency method.");
        logger.info("Player: " + player.getName() + ", Amount: " + amount);
    
        if (player.getInventory().getContents() != null) {
            logger.info("Player inventory is not null.");
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && validateCurrency(item)) {
                    logger.info("Valid currency item found: " + item.toString());
                    itemsToRemove.add(item);
                    count += item.getAmount();
                    logger.info("Current count: " + count);
                    if (count >= amount) {
                        logger.info("Required amount reached. Breaking loop.");
                        break;
                    }
                } else if (item != null) {
                    logger.info("Invalid item: " + item.toString());
                }
            }
        } else {
            logger.info("Player inventory is null.");
        }
    
        // Remove the items from the player's inventory
        if (count >= amount) {
            int remaining = amount;
            logger.info("Sufficient currency found. Starting removal process.");
            for (ItemStack item : itemsToRemove) {
                int itemAmount = item.getAmount();
                logger.info("Processing item: " + item.toString() + ", Item Amount: " + itemAmount + ", Remaining: " + remaining);
                if (itemAmount <= remaining) {
                    player.getInventory().removeItem(item);
                    remaining -= itemAmount;
                    logger.info("Item removed completely. Remaining amount to remove: " + remaining);
                } else {
                    item.setAmount(itemAmount - remaining);
                    logger.info(String.valueOf(itemAmount - remaining));
                    logger.info("Item partially removed. Remaining amount set to zero.");
                    remaining = 0;
                }
     
                if (remaining <= 0) {
                    logger.info("All required items removed. Breaking loop.");
                    break;
                }
            }
            logger.info("Deposit successful.");
            return true;
        } else {
            logger.info("Insufficient currency. Deposit failed.");
            return false;
        }
    }

    public boolean withdrawCurrency(Player player, int amount) {
        FileConfiguration config = plugin.getConfig();

        String CURRENCY_MATERIAL = config.getString("currency.item");
        int CURRENCY_MODEL_DATA = config.getInt("currency.customModelData");
        String CURRENCY_NAME = config.getString("currency.name");
        List<String> CURRENCY_LORE = config.getStringList("currency.lore");
    
        List<ItemStack> currencyItems = new ArrayList<>();
    
        // Create ItemStacks of 64 until remaining amount is less than 64
        while (amount > 64) {
            ItemStack currencyItem = new ItemStack(Material.getMaterial(CURRENCY_MATERIAL), 64);
            ItemMeta meta = currencyItem.getItemMeta();
            
            if (meta != null) {
                meta.itemName(mm.deserialize(CURRENCY_NAME));
                meta.setCustomModelData(CURRENCY_MODEL_DATA);
    
                List<Component> lore = new ArrayList<>();
                for (String rawLoreLine : CURRENCY_LORE) {
                    lore.add(mm.deserialize(rawLoreLine));
                }
    
                if (!lore.isEmpty()) {
                    meta.lore(lore);
                }
    
                currencyItem.setItemMeta(meta);
            }
            
            currencyItems.add(currencyItem);
            amount -= 64;
        }
    
        // Add the remaining amount as the last ItemStack
        if (amount > 0) {
            ItemStack currencyItem = new ItemStack(Material.getMaterial(CURRENCY_MATERIAL), amount);
            ItemMeta meta = currencyItem.getItemMeta();
            
            if (meta != null) {
                meta.itemName(mm.deserialize(CURRENCY_NAME));
                meta.setCustomModelData(CURRENCY_MODEL_DATA);
    
                List<Component> lore = new ArrayList<>();
                for (String rawLoreLine : CURRENCY_LORE) {
                    lore.add(mm.deserialize(rawLoreLine));
                }
    
                if (!lore.isEmpty()) {
                    meta.lore(lore);
                }
    
                currencyItem.setItemMeta(meta);
            }
            
            currencyItems.add(currencyItem);
        }
    
        // Attempt to add the items to the player's inventory
        HashMap<Integer, ItemStack> remainingItems = new HashMap<>();
        for (ItemStack item : currencyItems) {
            remainingItems.putAll(player.getInventory().addItem(item));
        }
    
        if (remainingItems.isEmpty()) {
            logger.info("Withdraw successful.");
            return true;
        } else {
            // If there are remaining items, it means the inventory was full
            for (ItemStack item : remainingItems.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            logger.info("Withdraw successful, but player's inventory was full. Dropped remaining items at player's location.");
            return true;
        }
    }
    
}    