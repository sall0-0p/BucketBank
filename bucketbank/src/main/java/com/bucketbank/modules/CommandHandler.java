package com.bucketbank.modules;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.bucketbank.commands.bucketfinance.AboutCommand;
import com.bucketbank.commands.bucketfinance.BalanceCommand;
import com.bucketbank.commands.bucketfinance.PayCommand;
import com.bucketbank.commands.bucketfinance.ReloadConfig;
import com.bucketbank.commands.bucketfinance.account.CreateAccountCommand;
import com.bucketbank.commands.bucketfinance.account.GiveAccess;
import com.bucketbank.commands.bucketfinance.account.HistoryCommand;
import com.bucketbank.commands.bucketfinance.account.ReinstateAccountCommand;
import com.bucketbank.commands.bucketfinance.account.RemoveAccess;
import com.bucketbank.commands.bucketfinance.account.SuspendAccountCommand;
import com.bucketbank.commands.bucketfinance.account.balance.GetBalanceCommand;
import com.bucketbank.commands.bucketfinance.account.balance.SetBalanceCommand;
import com.bucketbank.commands.bucketfinance.user.AboutUserCommand;
import com.bucketbank.commands.bucketfinance.user.AccountsCommand;
import com.bucketbank.commands.bucketfinance.user.CreateUserCommand;
import com.bucketbank.commands.bucketfinance.user.DeleteUserCommand;
import com.bucketbank.commands.bucketfinance.user.LimitAccountsCommand;
import com.bucketbank.commands.bucketfinance.user.ReinstateUserCommand;
import com.bucketbank.commands.bucketfinance.user.SuspendUserCommand;

public class CommandHandler implements CommandExecutor {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandHandler() {
        // main
        commands.put("about", new AboutCommand());
        commands.put("reload", new ReloadConfig());
        commands.put("balance", new BalanceCommand());
        commands.put("pay", new PayCommand());

        // user
        commands.put("user about", new AboutUserCommand());
        commands.put("user create", new CreateUserCommand());
        commands.put("user delete", new DeleteUserCommand());
        commands.put("user suspend", new SuspendUserCommand());
        commands.put("user reinstate", new ReinstateUserCommand());
        commands.put("user limit set", new LimitAccountsCommand());
        commands.put("user accounts", new AccountsCommand());

        // account
        commands.put("account create", new CreateAccountCommand());
        commands.put("account balance get", new GetBalanceCommand());
        commands.put("account balance set", new SetBalanceCommand());
        commands.put("account history", new HistoryCommand());
        commands.put("account user add", new GiveAccess());
        commands.put("account user remove", new RemoveAccess());
        commands.put("account suspend", new SuspendAccountCommand());
        commands.put("account reinstate", new ReinstateAccountCommand());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("| Usage: /bucketfinance <command> [sub-command] [arguments]");
            return true;
        }

        StringBuilder subCommandPath = new StringBuilder(args[0]);
        Command command = commands.get(subCommandPath.toString());

        int index = 1;
        while (command == null && index < args.length) {
            subCommandPath.append(" ").append(args[index]);
            command = commands.get(subCommandPath.toString());
            index++;
        }

        if (command == null) {
            sender.sendMessage("| Unknown command. Type \"/help\" for help.");
            return true;
        }

        // Create a new array for the remaining arguments
        String[] subCommandArgs = new String[args.length - index];
        System.arraycopy(args, index, subCommandArgs, 0, args.length - index);

        command.execute(sender, subCommandArgs);

        return true;
    }
}
