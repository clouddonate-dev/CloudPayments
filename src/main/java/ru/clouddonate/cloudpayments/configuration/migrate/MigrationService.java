package ru.clouddonate.cloudpayments.configuration.migrate;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.CommentFileConfiguration;

import java.util.List;

@RequiredArgsConstructor
public final class MigrationService {

    private final Plugin plugin;
    private final List<MigratableFile> configs;

    public void migrateIfNeeded() {
        try {
            MigrationIO io = new MigrationIO(plugin);

            CommentConfigurationSection fsMain =
                    io.fs("config.yml");

            CommentConfigurationSection resMain =
                    io.resource("config.yml");

            int fsVersion = fsMain.getInt("configVersion", 1);
            int resVersion = resMain.getInt("configVersion", 1);

            if (fsVersion == resVersion) return;

            plugin.getLogger().info("Config migration: v" + fsVersion + " -> v" + resVersion);

            for (MigratableFile cfg : configs) {
                cfg.migrate(io, fsVersion);
            }

            io.backupFs(fsVersion);

            for (MigratableFile cfg : configs) {
                CommentFileConfiguration config = io.resource(cfg.fileName());
                io.save(cfg.fileName(), config);
            }

        } catch (Exception ignored) {
        }
    }
}