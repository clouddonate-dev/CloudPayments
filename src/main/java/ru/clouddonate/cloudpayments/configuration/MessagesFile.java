package ru.clouddonate.cloudpayments.configuration;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.migration.MigrationContext;
import ru.clouddonate.cloudpayments.util.TextUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class MessagesFile implements ConfigurationFile {

    private final List<String> noPerms = new ArrayList<>();

    private final List<String> cloudpaymentsUsage = new ArrayList<>();
    private final List<String> cloudpaymentsReload = new ArrayList<>();
    private final List<String> cloudpaymentsDebugOn = new ArrayList<>();
    private final List<String> cloudpaymentsDebugOff = new ArrayList<>();

    private final List<String> cartDisabled = new ArrayList<>();
    private final List<String> cartNotify = new ArrayList<>();


    @Override
    public @NotNull String fileName() {
        return "messages.yml";
    }

    @Override
    public void load(@NotNull CommentConfigurationSection config) {
        TextUtil.putAndColor(config, noPerms, "noPerms");

        TextUtil.putAndColor(config, cloudpaymentsUsage, "cloudpayments.usage");
        TextUtil.putAndColor(config, cloudpaymentsReload, "cloudpayments.reload");
        TextUtil.putAndColor(config, cloudpaymentsDebugOn, "cloudpayments.debugOn");
        TextUtil.putAndColor(config, cloudpaymentsDebugOff, "cloudpayments.debugOff");

        TextUtil.putAndColor(config, cartDisabled, "cart.disabled");
        TextUtil.putAndColor(config, cartNotify, "cart.notify");
    }

    @Override
    public void migrate(@NotNull MigrationContext ctx, int fromVersion, int toVersion) throws Exception {
        CommentConfigurationSection newConfig = ctx.resource(fileName());

        for(int version = fromVersion; version < toVersion; version++) {
            if(version == 1) {
                CommentConfigurationSection config = ctx.fs("config.yml");

                newConfig.set("noPerms", Collections.singletonList(config.getString("messages.noPermission", "")));
                newConfig.set("cloudpayments.reload", Collections.singletonList(config.getString("messages.reload", "").replace("{took}", "{time}") ));
                newConfig.set("cloudpayments.debugOn", Collections.singletonList(config.getString("messages.debug-enabled", "")));
                newConfig.set("cloudpayments.debugOff", Collections.singletonList(config.getString("messages.debug-disabled", "")));

                ctx.relocateCommonSections(config, newConfig);
            } else if(version == 2) {
                CommentConfigurationSection messages = ctx.fs("messages.yml");
                ctx.relocateCommonSections(messages, newConfig);
            }
        }
//        if(fromVersion < 3) { // 2 -> 3
//            FOR EXAMPLE
//        }
    }

}
