package ru.clouddonate.cloudpayments.configuration;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentFileConfiguration;
import ru.basher.configuration.migration.Migration;
import ru.clouddonate.cloudpayments.service.Service;

import java.io.File;

@Getter
public class ConfigurationService implements Service {

    private final Plugin plugin;

    private final ConfigFile configFile;
    private final MessagesFile messagesFile;
    private final CartFile cartFile;

    public ConfigurationService(Plugin plugin) {
        this.plugin = plugin;

        configFile = new ConfigFile(plugin);
        messagesFile = new MessagesFile();
        cartFile = new CartFile();
    }

    @Override
    public void enable() {
        Migration migration = new Migration.Builder().dataFolder(plugin.getDataFolder())
                        .versionFileName("config.yml").versionSection("configVersion")
                        .needBackup(true).addFiles(configFile, messagesFile, cartFile).build();
        migration.migrateIfNeeded();

        reload();
    }

    @Override
    public void reload() {
        saveAndLoadResource(configFile);
        saveAndLoadResource(messagesFile);
        saveAndLoadResource(cartFile);
    }

    private void saveAndLoadResource(@NotNull ConfigurationFile configurationFile) {
        try {
            File file = new File(plugin.getDataFolder(), configurationFile.fileName());
            if(!file.exists()) plugin.saveResource(configurationFile.fileName(), false);

            CommentFileConfiguration config = new CommentFileConfiguration();
            config.load(file);
            configurationFile.load(config);
        } catch (Exception ignored) {
        }
    }

}
