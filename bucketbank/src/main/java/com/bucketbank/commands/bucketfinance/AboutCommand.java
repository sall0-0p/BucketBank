package com.bucketbank.commands.bucketfinance;

import org.bukkit.command.CommandSender;

import com.bucketbank.modules.Command;

public class AboutCommand implements Command {
    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("Hello World! BucketFinance v1.0");
    }
}
