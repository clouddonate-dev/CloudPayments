package ru.clouddonate.cloudpayments.shop;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.clouddonate.cloudpayments.announcement.AnnouncementService;
import ru.clouddonate.cloudpayments.api.event.PurchaseApproveEvent;
import ru.clouddonate.cloudpayments.cart.CartService;
import ru.clouddonate.cloudpayments.configuration.CartFile;
import ru.clouddonate.cloudpayments.configuration.ConfigFile;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.messenger.MessengerService;
import ru.clouddonate.cloudpayments.service.Service;
import ru.clouddonate.cloudpayments.shop.result.GetResult;
import ru.clouddonate.cloudpayments.storage.LocalStorageService;
import ru.clouddonate.cloudpayments.util.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ShopService implements Service {

    private static final String GET_URL = "https://api.cdonate.ru/api/v1/shops/{shopId}/purchases/pending?server_id={serverId}";
    private static final String POST_URL = "https://api.cdonate.ru/api/v1/shops/{shopId}/purchases/{purchaseId}/approve";

    private final Plugin plugin;
    private final ConfigFile configFile;
    private final CartFile cartFile;

    private final AnnouncementService announcementService;
    private final CartService cartService;
    private final MessengerService messengerService;
    private final LocalStorageService localStorageService;

    private ShopSettings shopSettings;
    private BukkitTask task;

    public ShopService(Plugin plugin, ConfigurationService configurationService,
                       AnnouncementService announcementService, CartService cartService,
                       MessengerService messengerService, LocalStorageService localStorageService) {
        this.plugin = plugin;
        this.configFile = configurationService.getConfigFile();
        this.cartFile = configurationService.getCartFile();

        this.announcementService = announcementService;
        this.cartService = cartService;
        this.messengerService = messengerService;
        this.localStorageService = localStorageService;
    }

    @Override
    public void enable() {
        reload();
        task = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            private int timePassed = 0;

            @Override
            public void run() {
                if (timePassed >= configFile.getSettingsRequestDelay()) {
                    request();
                    timePassed = 0;
                } else {
                    timePassed++;
                }
            }
        }, 20, 20);
    }

    @Override
    public void reload() {
        shopSettings = new ShopSettings(configFile.getSettingsShopShopId(), configFile.getSettingsShopShopKey(), configFile.getSettingsShopServerId());
    }

    @Override
    public void disable() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void request() {
        try {
            GetResult[] getResults = requestPurchases();
            if (getResults == null) {
                if (configFile.isSettingsDebugMode()) plugin.getLogger().info("Not correct GET result format (null)");
                return;
            }
            if (configFile.isSettingsDebugMode()) plugin.getLogger().info("GET return " + getResults.length + " results");

            List<GetResult> goodResults = new ArrayList<>();
            for (GetResult result : getResults) {
                int responseCode = sendSuccessHandle(result);
                if (responseCode != 204) {
                    if (configFile.isSettingsDebugMode()) plugin.getLogger().warning("Failed to approve purchase ID " + result.getId() + ". Response code: " + responseCode);
                } else {
                    sendMessageToMessenger(result);
                    localStorageService.addPayment(result);

                    if(!cartFile.isEnabled()) {
                        goodResults.add(result);
                    } else {
                        if(cartFile.isOnlyForOffline()) {
                            if(plugin.getServer().getPlayer(result.getNickname()) != null) {
                                goodResults.add(result);
                            } else {
                                addToCart(result);
                            }
                        } else {
                            addToCart(result);
                        }
                    }
                    PurchaseApproveEvent event = new PurchaseApproveEvent(result);
                    plugin.getServer().getPluginManager().callEvent(event);
                }
            }

            if (!goodResults.isEmpty()) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    ConsoleCommandSender sender = plugin.getServer().getConsoleSender();
                    for (GetResult result : goodResults) {
                        for (String command : result.getCommands()) {
                            if (configFile.isSettingsDebugMode()) plugin.getLogger().info("Executing command: " + command);
                            plugin.getServer().dispatchCommand(sender, command
                                    .replace("{user}", result.getNickname())
                                    .replace("{amount}", result.getAmount() + ""));
                        }
                        announcementService.handle(result);
                    }
                });
            }
        } catch (Exception e) {
            if (configFile.isSettingsDebugMode()) plugin.getLogger().log(Level.WARNING, "Request to donate site is failed.", e.getMessage());
        }
    }

    private void addToCart(@NotNull GetResult result) {
        List<String> commands = new ArrayList<>();
        for(String cmd : result.getCommands()) {
            commands.add(cmd.replace("{user}", result.getNickname())
                    .replace("{amount}", result.getAmount() + ""));
        }
        cartService.addToCart(result.getNickname(), result.getName(), result.getPrice(), result.getAmount(), commands);
    }

    private void sendMessageToMessenger(@NotNull GetResult result) {
        messengerService.sendMessage("✅ Пришёл платёж: ID " + result.getId() + "\n\n"
                + "❓ Информация:\n👤 Никнейм: " + result.getNickname() + "\n"
                + "🛒 Товар: " + result.getName() + " (кол-во: x" + result.getAmount() + ")\n"
                + "🔥 Пришло с учётом комиссии сервиса: " + result.getPrice() + " рублей\n\n"
                + "❤️ Благодарим за использование CloudDonate!");
    }

    private @Nullable GetResult[] requestPurchases() throws Exception {
        String urlStr = GET_URL.replace("{shopId}", shopSettings.getShopId())
                .replace("{serverId}", shopSettings.getServerId());
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("X-Shop-Key", shopSettings.getShopKey());
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        int responseCode = connection.getResponseCode();

        GetResult[] result = null;
        if (responseCode == 200) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            result = JsonUtil.deserialize(response.toString(), GetResult[].class);
        } else {
            if (configFile.isSettingsDebugMode()) plugin.getLogger().log(Level.WARNING, "Failed to fetch data. Response code: " + responseCode);
        }
        connection.disconnect();
        return result;
    }

    public int sendSuccessHandle(@NotNull GetResult result) throws IOException {
        String postUrl = POST_URL.replace("{shopId}", shopSettings.getShopId()).replace("{purchaseId}", result.getId() + "");
        HttpURLConnection postConnection = (HttpURLConnection) new URL(postUrl).openConnection();
        postConnection.setRequestMethod("POST");
        postConnection.setRequestProperty("X-Shop-Key", shopSettings.getShopKey());
        postConnection.setRequestProperty("Content-Type", "application/json");
        postConnection.setDoOutput(true);
        postConnection.setConnectTimeout(5000);
        postConnection.setReadTimeout(5000);
        try (OutputStream os = postConnection.getOutputStream();) {
            byte[] input = "{}".getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return postConnection.getResponseCode();
    }


}
