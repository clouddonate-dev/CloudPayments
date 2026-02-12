package ru.clouddonate.cloudpayments.messenger;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.clouddonate.cloudpayments.configuration.ConfigFile;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.messenger.telegram.TelegramMessenger;
import ru.clouddonate.cloudpayments.service.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@Getter
public final class MessengerService implements Service {

    private final Plugin plugin;
    private final ConfigFile configFile;

    private final List<Messenger> connectedMessengers = new ArrayList<>();

    public MessengerService(Plugin plugin, ConfigurationService configurationService) {
        this.plugin = plugin;
        this.configFile = configurationService.getConfigFile();
    }

    @Override
    public void enable() {
        reload();
    }

    @Override
    public void reload() {
        disable();

        if (configFile.isMessengersTelegramEnabled()) {
            register(new TelegramMessenger(plugin.getLogger(), configFile.getMessengersTelegramApiToken(), configFile.getMessengersTelegramIds()));
        }
    }

    @Override
    public void disable() {
        connectedMessengers.forEach(Messenger::disconnect);
        connectedMessengers.clear();
    }

    private void register(@NotNull Messenger... messengers) {
        for (Messenger messenger : messengers) {
            try {
                if (configFile.isSettingsDebugMode()) plugin.getLogger().log(Level.INFO, "Registering Messenger " + messenger.getClass().getSimpleName());
                connectedMessengers.add(messenger);
                messenger.connect();
                if (configFile.isSettingsDebugMode()) plugin.getLogger().log(Level.INFO, "Messenger " + messenger.getClass().getSimpleName() + " connected");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Messenger cannot be connect: " + messenger, e);
            }
        }
    }

    public void sendMessage(@NotNull String message) {
        for(Messenger messenger : connectedMessengers){
            messenger.sendMessage(message);
        }
    }

}
