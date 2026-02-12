package ru.clouddonate.cloudpayments.command;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.clouddonate.cloudpayments.announcement.AnnouncementService;
import ru.clouddonate.cloudpayments.cart.CartService;
import ru.clouddonate.cloudpayments.cart.menu.CartMenu;
import ru.clouddonate.cloudpayments.cart.model.PlayerCart;
import ru.clouddonate.cloudpayments.configuration.CartFile;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.configuration.MessagesFile;
import ru.clouddonate.cloudpayments.menu.MenuService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CartCommand implements CommandExecutor, TabCompleter {

    private static final String COMMAND = "cart";
    private static final String PERMISSION = "cloudpayments.command.cart";

    private final ConfigurationService configurationService;
    private final CartService cartService;
    private final AnnouncementService announcementService;

    private final CartFile cartFile;
    private final MessagesFile messagesFile;

    public CartCommand(ConfigurationService configurationService, CartService cartService,
                       AnnouncementService announcementService) {
        this.configurationService = configurationService;
        this.cartService = cartService;
        this.announcementService = announcementService;

        cartFile = configurationService.getCartFile();
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be executed by players");
            return true;
        }
        if (!sender.hasPermission(PERMISSION)) {
            for (String msg : messagesFile.getNoPerms()) sender.sendMessage(msg);
            return true;
        }
        if (!cartFile.isEnabled()) {
            for (String msg : messagesFile.getCartDisabled()) sender.sendMessage(msg);
            return true;
        }
        Player player = (Player) sender;
        CompletableFuture.runAsync(() -> {
            PlayerCart cart = cartService.getCartOrGenerate(player.getName());
            MenuService.openMenuAsync((Player) sender, () -> new CartMenu(configurationService, announcementService, cart));
        });
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }

}
