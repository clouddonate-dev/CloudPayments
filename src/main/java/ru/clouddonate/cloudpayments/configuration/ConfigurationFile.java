package ru.clouddonate.cloudpayments.configuration;

import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.migration.MigratableFile;

public interface ConfigurationFile extends MigratableFile {

    void load(@NotNull CommentConfigurationSection config);

}
