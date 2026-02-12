package ru.clouddonate.cloudpayments.configuration.migrate;

import com.google.common.base.Charsets;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.CommentFileConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public final class MigrationIO {

    private final Plugin plugin;
    private final File dataFolder;

    private final Map<String, CommentConfigurationSection> fsNodes = new HashMap<>();
    private final Map<String, CommentFileConfiguration> resourceNodes = new HashMap<>();

    public MigrationIO(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
    }

    public @NotNull CommentConfigurationSection fs(@NotNull String fileName) throws Exception {
        if (fsNodes.containsKey(fileName)) return fsNodes.get(fileName);

        File file = new File(dataFolder, fileName);
        if (!file.exists()) throw new FileNotFoundException(fileName + " does not exist");

        CommentFileConfiguration config = new CommentFileConfiguration();
        config.load(file);
        fsNodes.put(fileName, config);
        return config;
    }

    public @NotNull CommentFileConfiguration resource(@NotNull String fileName) throws Exception {
        if (resourceNodes.containsKey(fileName)) return resourceNodes.get(fileName);

        try (InputStream in = plugin.getResource(fileName)) {
            if (in == null) throw new IllegalStateException("Resource not found: " + fileName);

            CommentFileConfiguration config = new CommentFileConfiguration();
            config.load(new InputStreamReader(in, Charsets.UTF_8));
            resourceNodes.put(fileName, config);
            return config;
        }
    }

    public void save(@NotNull String fileName, @NotNull CommentFileConfiguration config) {
        File file = new File(dataFolder, fileName);
        config.save(file);
    }

    public void backupFs(int fsVersion) {
        for (Map.Entry<String, CommentConfigurationSection> entry : fsNodes.entrySet()) {
            File file = new File(dataFolder, entry.getKey());
            file.renameTo(
                    new File(
                            dataFolder, file.getName().replace(".yml", "-backup-v" + fsVersion + ".yml")
                    )
            );
        }
    }

}