package ru.clouddonate.cloudpayments.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.basher.configuration.CommentFileConfiguration;
import ru.basher.configuration.migration.MigrationContext;
import ru.clouddonate.cloudpayments.util.TextUtil;

import java.io.File;
import java.util.*;

@Getter
@RequiredArgsConstructor
public class ConfigFile implements ConfigurationFile {

    private final Plugin plugin;

    private boolean settingsDebugMode;
    private int settingsRequestDelay;
    private boolean settingsEnableMetrics;

    private String settingsShopShopId;
    private String settingsShopShopKey;
    private String settingsShopServerId;

    private boolean logsPaymentsEnabled;
    private String logsPaymentsFormat;

    private boolean messengersTelegramEnabled;
    private String messengersTelegramApiToken;
    private final List<String> messengersTelegramIds = new ArrayList<>();

    private final Map<Integer, List<String>> inGameAnnouncements = new HashMap<>();

    @Override
    public @NotNull String fileName() {
        return "config.yml";
    }

    @Override
    public void load(@NotNull CommentConfigurationSection config) {
        settingsDebugMode = config.getBoolean("settings.debugMode", false);
        settingsRequestDelay = Math.max(config.getInt("settings.requestDelay", 0), 20);
        settingsEnableMetrics = config.getBoolean("settings.enableMetrics", true);

        settingsShopShopId = config.getString("settings.shop.shopId", "");
        settingsShopShopKey = config.getString("settings.shop.shopKey", "");
        settingsShopServerId = config.getString("settings.shop.serverId", "");

        logsPaymentsEnabled = config.getBoolean("logs.payments.enabled", false);
        logsPaymentsFormat = config.getString("logs.payments.format", "");

        messengersTelegramEnabled = config.getBoolean("messengers.telegram.enabled", false);
        messengersTelegramApiToken = config.getString("messengers.telegram.apiToken", "");
        messengersTelegramIds.clear();
        messengersTelegramIds.addAll(config.getStringList("messengers.telegram.ids"));

        inGameAnnouncements.clear();
        CommentConfigurationSection inGameAnnouncementsSec = config.getConfigurationSection("inGameAnnouncements");
        if (inGameAnnouncementsSec != null) {
            for (String key : inGameAnnouncementsSec.getMap().keySet()) {
                CommentConfigurationSection keySec = inGameAnnouncementsSec.getConfigurationSection(key);
                if (keySec == null) continue;
                int identifier;
                try {
                    identifier = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }
                List<String> actions = new ArrayList<>();
                for (String str : keySec.getStringList("actions")) {
                    actions.add(TextUtil.toColor(str));
                }
                inGameAnnouncements.put(identifier, actions);
            }
        }
    }

    @Override
    public void migrate(@NotNull MigrationContext ctx, int fromVersion, int toVersion) throws Exception {
        CommentConfigurationSection newConfig = ctx.resource(fileName());

        for(int version = fromVersion; version < toVersion; version++) {
            if(version == 1) {
                CommentConfigurationSection config = ctx.fs("config.yml");

                newConfig.set("settings.debugMode", config.getBoolean("settings.debug-mode", false));
                newConfig.set("settings.requestDelay", Math.max(config.getInt("settings.request-delay", 0), 20));

                newConfig.set("settings.shop.shopId", config.getString("settings.shop.shop-id", ""));
                newConfig.set("settings.shop.shopKey", config.getString("settings.shop.shop-key", ""));
                newConfig.set("settings.shop.serverId", config.getString("settings.shop.server-id", ""));

                newConfig.set("logs.payments.enabled", config.getBoolean("local-storage.payments.enabled", false));
                newConfig.set("logs.payments.format", config.getString("local-storage.payments.format", "")
                        .replace("<date>", "{date}")
                        .replace("<time>", "{time}")
                        .replace("<nickname>", "{playerName}")
                        .replace("<product_name>", "{product}")
                        .replace("<count>", "{amount}")
                        .replace("<price>", "{price}")
                        .replace("<payment_id>", "{paymentId}")
                );

                newConfig.set("messengers.telegram.enabled", config.getBoolean("messengers.telegram.enabled", false));
                newConfig.set("messengers.telegram.apiToken", config.getString("messengers.telegram.api-token", ""));

                newConfig.set("messengers.telegram.ids", config.getStringList("messengers.telegram.ids"));

                CommentConfigurationSection inGameAnnouncementsSec = newConfig.getConfigurationSection("inGameAnnouncements");
                CommentConfigurationSection oldInGameAnnouncementsSec = config.getConfigurationSection("in-game-announcements");
                if (inGameAnnouncementsSec != null && oldInGameAnnouncementsSec != null) {
                    int i = 12345;
                    for (String key : oldInGameAnnouncementsSec.getMap().keySet()) {
                        List<String> actions = oldInGameAnnouncementsSec.getStringList(key);
                        if(actions.isEmpty()) continue;
                        actions.replaceAll(string -> {
                            if (string.startsWith("{SOUND}")) {
                                string = string.replace("{SOUND}", "SOUND:") + ";1.0;1.0";
                            }
                            if (string.startsWith("{TITLE_MESSAGE}")) {
                                string = string.replace("{TITLE_MESSAGE}", "TITLE:").replace("::", ";");
                            }
                            return string
                                    .replace("{CHAT_MESSAGE}", "BROADCAST:")
                                    .replace("{ACTIONBAR_MESSAGE}", "ACTIONBAR:")
                                    .replace("{COMMAND}", "COMMAND:")
                                    .replace("<nickname>", "{playerName}")
                                    .replace("<price>", "{price}")
                                    .replace("<product>", "{product}")
                                    .replace("<count>", "{amount}");
                        });
                        inGameAnnouncementsSec.set(i + ".actions", actions);
                        i++;
                    }
                }
                ctx.relocateCommonSections(config, newConfig);
            } else if(version == 2) {
                CommentConfigurationSection config = ctx.fs("config.yml");
                ctx.relocateCommonSections(config, newConfig);
                CommentConfigurationSection inGameAnnouncementsSec = newConfig.getConfigurationSection("inGameAnnouncements");
                if(inGameAnnouncementsSec != null) {
                    int i = 12345;
                    Map<String, Object> map = inGameAnnouncementsSec.getMap();
                    Set<String> keysCopy = new HashSet<>(map.keySet());

                    for (String key : keysCopy) {
                        try {
                            Integer.parseInt(key);
                            Object obj = map.remove(key);
                            if(obj != null) {
                                map.put(i + "", obj);
                            }
                        } catch (NumberFormatException ignored) {
                        }
                        i++;
                    }
                }
            }
        }

    }

    public void setDebugMode(boolean enable) {
        settingsDebugMode = enable;
        File file = new File(plugin.getDataFolder(), fileName());
        if (!file.exists()) plugin.saveResource(fileName(), false);
        CommentFileConfiguration config = new CommentFileConfiguration();
        config.load(file);
        config.set("settings.debugMode", enable);
        config.save(file);
    }

}
