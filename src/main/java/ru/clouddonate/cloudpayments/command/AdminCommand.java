package ru.clouddonate.cloudpayments.command;

import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.clouddonate.cloudpayments.CloudPaymentsPlugin;
import ru.clouddonate.cloudpayments.configuration.ConfigFile;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.configuration.MessagesFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminCommand implements CommandExecutor, TabCompleter {

    private static final String COMMAND = "cloudpayments";
    private static final String PERMISSION = "cloudpayments.command.cloudpayments";

    private final CloudPaymentsPlugin plugin;

    private final ConfigFile configFile;
    private final MessagesFile messagesFile;

    public AdminCommand(CloudPaymentsPlugin plugin, ConfigurationService configurationService) {
        this.plugin = plugin;

        configFile = configurationService.getConfigFile();
        messagesFile = configurationService.getMessagesFile();
    }

    public void register(@NotNull JavaPlugin plugin) {
        PluginCommand command = plugin.getServer().getPluginCommand(COMMAND);
        if (command == null) command = plugin.getCommand(COMMAND);
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            for (String msg : messagesFile.getNoPerms()) sender.sendMessage(msg);
            return true;
        }
        if (args.length == 0) {
            for (String msg : messagesFile.getCloudpaymentsUsage()) sender.sendMessage(msg);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            long start = System.currentTimeMillis();
            plugin.reload();
            long diff = System.currentTimeMillis() - start;
            for (String msg : messagesFile.getCloudpaymentsReload())
                sender.sendMessage(
                        msg.replace("{time}", diff + "")
                );
        } else if (args[0].equalsIgnoreCase("debug")) {
            if (args.length < 2) {
                for (String msg : messagesFile.getCloudpaymentsUsage()) sender.sendMessage(msg);
                return true;
            }
            String toggle = args[1];
            if (toggle.equalsIgnoreCase("on")) {
                configFile.setDebugMode(true);
                for (String msg : messagesFile.getCloudpaymentsDebugOn()) sender.sendMessage(msg);
            } else if (toggle.equalsIgnoreCase("off")) {
                configFile.setDebugMode(false);
                for (String msg : messagesFile.getCloudpaymentsDebugOff()) sender.sendMessage(msg);
            } else {
                for (String msg : messagesFile.getCloudpaymentsUsage()) sender.sendMessage(msg);
            }
        } else {
            for (String msg : messagesFile.getCloudpaymentsUsage()) sender.sendMessage(msg);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) return new ArrayList<>();
        if (args.length < 2) return Arrays.asList("reload", "debug");

        if (args[0].equalsIgnoreCase("debug")) {
            if (args.length == 2) return Arrays.asList("on", "off");
        }
        return new ArrayList<>();
    }

}
