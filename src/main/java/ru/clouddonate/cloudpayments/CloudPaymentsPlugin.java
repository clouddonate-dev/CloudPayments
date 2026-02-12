package ru.clouddonate.cloudpayments;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import ru.clouddonate.cloudpayments.announcement.AnnouncementService;
import ru.clouddonate.cloudpayments.cart.CartService;
import ru.clouddonate.cloudpayments.command.CommandService;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.menu.MenuService;
import ru.clouddonate.cloudpayments.messenger.MessengerService;
import ru.clouddonate.cloudpayments.metric.Metrics;
import ru.clouddonate.cloudpayments.shop.ShopService;
import ru.clouddonate.cloudpayments.storage.LocalStorageService;

import java.io.File;

@Getter
public final class CloudPaymentsPlugin extends JavaPlugin {

    public static final int METRICS_SERVICE_ID = 27282;

    @Getter
    private static CloudPaymentsPlugin plugin;

    private ConfigurationService configurationService;
    private AnnouncementService announcementService;
    private CartService cartService;
    private MenuService menuService;
    private MessengerService messengerService;
    private LocalStorageService localStorageService;
    private ShopService shopService;
    private CommandService commandService;

    private Metrics metrics;

    @Override
    public void onEnable() {
        plugin = this;
        if (!createDirs()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        configurationService = new ConfigurationService(this);
        configurationService.enable();
        announcementService = new AnnouncementService(this, configurationService);
        announcementService.enable();
        cartService = new CartService(this, configurationService);
        cartService.enable();
        menuService = new MenuService(this);
        menuService.enable();
        messengerService = new MessengerService(this, configurationService);
        messengerService.enable();
        localStorageService = new LocalStorageService(this, configurationService);
        localStorageService.enable();
        shopService = new ShopService(this, configurationService, announcementService, cartService,
                messengerService, localStorageService);
        shopService.enable();
        commandService = new CommandService(this, configurationService, cartService, announcementService);
        commandService.enable();

        metrics = new Metrics(this, METRICS_SERVICE_ID);
    }

    private boolean createDirs() {
        if (!getDataFolder().exists() && !getDataFolder().mkdirs()) return false;
        File localStorageDir = new File(getDataFolder(), "local");
        if (!localStorageDir.exists() && !localStorageDir.mkdirs()) return false;
        return true;
    }

    public void reload() {
        configurationService.reload();
        announcementService.reload();
        cartService.reload();
        menuService.reload();
        messengerService.reload();
        localStorageService.reload();
        shopService.reload();
        commandService.reload();
    }

    @Override
    public void onDisable() {
        if (metrics != null) metrics.shutdown();

        if (commandService != null) commandService.disable();
        if (shopService != null) shopService.disable();
        if (localStorageService != null) localStorageService.disable();
        if (messengerService != null) messengerService.disable();
        if (menuService != null) menuService.disable();
        if (cartService != null) cartService.disable();
        if (announcementService != null) announcementService.disable();
        if (configurationService != null) configurationService.disable();
    }

}
