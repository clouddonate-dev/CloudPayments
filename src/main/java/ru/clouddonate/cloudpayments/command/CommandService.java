package ru.clouddonate.cloudpayments.command;

import org.bukkit.plugin.java.JavaPlugin;
import ru.clouddonate.cloudpayments.CloudPaymentsPlugin;
import ru.clouddonate.cloudpayments.announcement.AnnouncementService;
import ru.clouddonate.cloudpayments.cart.CartService;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.service.Service;

public class CommandService implements Service {

    private final JavaPlugin plugin;

    private final AdminCommand adminCommand;
    private final CartCommand cartCommand;

    public CommandService(CloudPaymentsPlugin plugin, ConfigurationService configurationService,
                          CartService cartService, AnnouncementService announcementService) {
        this.plugin = plugin;

        adminCommand = new AdminCommand(plugin, configurationService);
        cartCommand = new CartCommand(configurationService, cartService, announcementService);
    }

    @Override
    public void enable() {
        adminCommand.register(plugin);
        cartCommand.register(plugin);
    }

}
