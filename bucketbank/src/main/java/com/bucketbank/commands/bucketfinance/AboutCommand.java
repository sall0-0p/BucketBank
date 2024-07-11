package com.bucketbank.commands.bucketfinance;

import org.bukkit.command.CommandSender;

import com.bucketbank.App;
import com.bucketbank.modules.Command;
import com.bucketbank.modules.Messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AboutCommand implements Command {
    private static final App plugin = App.getPlugin();
    private static final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void execute(CommandSender sender, String[] args) {
        Component parsed = mm.deserialize(Messages.getString("about"));

        sender.sendMessage(parsed);
    }
}
