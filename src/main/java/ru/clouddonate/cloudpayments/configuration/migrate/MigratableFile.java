package ru.clouddonate.cloudpayments.configuration.migrate;

import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;

public interface MigratableFile {

    @NotNull String fileName();

    void load(@NotNull CommentConfigurationSection config);

    void migrate(@NotNull MigrationIO io, int fromVersion) throws Exception;

}
