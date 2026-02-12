package ru.clouddonate.cloudpayments.api;

import org.jetbrains.annotations.NotNull;
import ru.clouddonate.cloudpayments.CloudPaymentsPlugin;
import ru.clouddonate.cloudpayments.announcement.AnnouncementService;
import ru.clouddonate.cloudpayments.cart.CartService;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.messenger.MessengerService;
import ru.clouddonate.cloudpayments.storage.LocalStorageService;

public class CloudPaymentsApi {

    private static CloudPaymentsPlugin plugin;

    public static void setPlugin(@NotNull CloudPaymentsPlugin plugin) {
        if(CloudPaymentsApi.plugin != null) {
            CloudPaymentsApi.plugin.getLogger().warning("CloudPaymentsPlugin has been already initialized!");
        } else {
            CloudPaymentsApi.plugin = plugin;
        }
    }

    public static @NotNull AnnouncementService getAnnouncementService() {
        return plugin.getAnnouncementService();
    }

    public static @NotNull ConfigurationService getConfigurationService() {
        return plugin.getConfigurationService();
    }

    public static @NotNull MessengerService getMessengerService() {
        return plugin.getMessengerService();
    }

    public static @NotNull LocalStorageService getLocalStorageService() {
        return plugin.getLocalStorageService();
    }

    public static @NotNull CartService getCartService() {
        return plugin.getCartService();
    }


}
