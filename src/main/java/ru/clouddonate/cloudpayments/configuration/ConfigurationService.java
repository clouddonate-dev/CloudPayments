package ru.clouddonate.cloudpayments.configuration;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentFileConfiguration;
import ru.clouddonate.cloudpayments.configuration.migrate.MigratableFile;
import ru.clouddonate.cloudpayments.configuration.migrate.MigrationService;
import ru.clouddonate.cloudpayments.service.Service;

import java.io.File;
import java.util.Arrays;

@Getter
public class ConfigurationService implements Service {

    private final Plugin plugin;
    private final MigrationService migrationService;

    private final ConfigFile configFile;
    private final MessagesFile messagesFile;
    private final CartFile cartFile;

    public ConfigurationService(Plugin plugin) {
        this.plugin = plugin;

        configFile = new ConfigFile(plugin);
        messagesFile = new MessagesFile();
        cartFile = new CartFile();

        migrationService = new MigrationService(plugin,
                Arrays.asList(configFile, messagesFile, cartFile)
        );
    }

    @Override
    public void enable() {
        migrationService.migrateIfNeeded();

        reload();
    }

    @Override
    public void reload() {
        saveAndLoadResource(configFile);
        saveAndLoadResource(messagesFile);
        saveAndLoadResource(cartFile);
    }

    private void saveAndLoadResource(@NotNull MigratableFile migratableFile) {
        try {
            File file = new File(plugin.getDataFolder(), migratableFile.fileName());
            if(!file.exists()) plugin.saveResource(migratableFile.fileName(), false);

            CommentFileConfiguration config = new CommentFileConfiguration();
            config.load(file);
            migratableFile.load(config);
        } catch (Exception ignored) {
        }
    }

}
