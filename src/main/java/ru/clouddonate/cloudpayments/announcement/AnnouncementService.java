package ru.clouddonate.cloudpayments.announcement;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.clouddonate.cloudpayments.configuration.ConfigFile;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.shop.result.GetResult;
import ru.clouddonate.cloudpayments.service.Service;
import ru.clouddonate.cloudpayments.util.TextUtil;

import java.util.List;

public class AnnouncementService implements Service {

    private final Plugin plugin;
    private final ConfigFile configFile;

    public AnnouncementService(Plugin plugin, ConfigurationService configurationService) {
        this.plugin = plugin;
        this.configFile = configurationService.getConfigFile();
    }

    public void handle(@NotNull GetResult result) {
        handle(result.getProduct_id(), result.getName(), result.getNickname(), result.getPrice(), result.getAmount());
    }

    public void handle(int productId, @NotNull String productName, @NotNull String playerName, int price, int amount) {
        List<String> actions = configFile.getInGameAnnouncements().get(productId);
        if (actions == null) return;

        Player buyer = plugin.getServer().getPlayer(playerName);
        for (String action : actions) {
            action = action.replace("{playerName}", playerName)
                    .replace("{price}", TextUtil.formatNumber(price))
                    .replace("{product}", productName)
                    .replace("{amount}", amount + "");

            if (action.startsWith("CHAT:")) {
                if (buyer != null) {
                    action = action.replace("CHAT:", "");
                    buyer.chat(action);
                }
            } else if (action.startsWith("MESSAGE:")) {
                if (buyer != null) {
                    action = action.replace("MESSAGE:", "");
                    buyer.sendMessage(action);
                }
            } else if (action.startsWith("TITLE:")) {
                if (buyer != null) {
                    action = action.replace("TITLE:", "");
                    String[] arr = action.split(";");
                    if (arr.length > 0) {
                        buyer.sendTitle(arr[0], arr.length > 1 ? arr[1] : "", 10, 70, 20);
                    }
                }
            } else if (action.startsWith("ACTIONBAR:")) {
                if (buyer != null) {
                    action = action.replace("ACTIONBAR:", "");
                    buyer.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(action));
                }
            } else if (action.startsWith("SOUND:")) {
                if (buyer != null) {
                    action = action.replace("SOUND:", "");
                    String[] arr = action.split(";");
                    if (arr.length == 3) {
                        try {
                            Sound sound = Sound.valueOf(arr[0]);
                            float volume = Float.parseFloat(arr[1]);
                            float pitch = Float.parseFloat(arr[2]);
                            buyer.playSound(buyer.getLocation(), sound, volume, pitch);
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else if (action.startsWith("BROADCAST:")) {
                action = action.replace("BROADCAST:", "");
                for (Player target : plugin.getServer().getOnlinePlayers()) {
                    if (target == null) continue;
                    target.sendMessage(action);
                }
            } else if (action.startsWith("BROADCAST_TITLE:")) {
                action = action.replace("BROADCAST_TITLE:", "");
                String[] arr = action.split(";");
                if (arr.length > 0) {
                    String title = arr[0];
                    String subtitle = arr.length > 1 ? arr[1] : "";
                    for (Player target : plugin.getServer().getOnlinePlayers()) {
                        if (target == null) continue;
                        target.sendTitle(title, subtitle, 10, 70, 20);
                    }
                }
            } else if (action.startsWith("BROADCAST_ACTIONBAR:")) {
                action = action.replace("BROADCAST_ACTIONBAR:", "");
                BaseComponent[] component = TextComponent.fromLegacyText(action);
                for (Player target : plugin.getServer().getOnlinePlayers()) {
                    if (target == null) continue;
                    target.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                }
            } else if (action.startsWith("BROADCAST_SOUND:")) {
                action = action.replace("BROADCAST_SOUND:", "");
                String[] arr = action.split(";");
                if (arr.length == 3) {
                    try {
                        Sound sound = Sound.valueOf(arr[0]);
                        float volume = Float.parseFloat(arr[1]);
                        float pitch = Float.parseFloat(arr[2]);
                        for (Player target : plugin.getServer().getOnlinePlayers()) {
                            if (target == null) continue;
                            target.playSound(target.getLocation(), sound, volume, pitch);
                        }
                    } catch (Exception ignored) {
                    }
                }
            } else if (action.startsWith("COMMAND:")) {
                if (buyer != null) {
                    action = action.replace("COMMAND:", "");
                    buyer.chat("/" + action);
                }
            } else if (action.startsWith("CONSOLE:")) {
                action = action.replace("CONSOLE:", "");
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), action);
            }
        }
    }


}
