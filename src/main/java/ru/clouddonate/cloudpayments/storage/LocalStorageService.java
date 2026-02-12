package ru.clouddonate.cloudpayments.storage;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.clouddonate.cloudpayments.configuration.ConfigFile;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.shop.result.GetResult;
import ru.clouddonate.cloudpayments.service.Service;
import ru.clouddonate.cloudpayments.util.FileUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LocalStorageService implements Service {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final Plugin plugin;
    private final ConfigFile configFile;

    public LocalStorageService(Plugin plugin, ConfigurationService configurationService) {
        this.plugin = plugin;
        this.configFile = configurationService.getConfigFile();
    }

    public void addPayment(@NotNull GetResult getResult) {
        if (configFile.isLogsPaymentsEnabled()) {
            String time = Instant.ofEpochMilli(System.currentTimeMillis())
                    .atZone(ZoneId.systemDefault())
                    .format(formatter);
            String text = configFile.getLogsPaymentsFormat()
                    .replace("{date}", LocalDate.now().format(DateTimeFormatter.ISO_DATE))
                    .replace("{time}", time)
                    .replace("{playerName}", getResult.getAmount() + "")
                    .replace("{product}", getResult.getNickname())
                    .replace("{amount}", getResult.getName())
                    .replace("{price}", getResult.getPrice() + "")
                    .replace("{paymentId}", getResult.getId() + "");
            FileUtil.appendToFile(plugin.getDataFolder() + "/local/payments.txt", text);
        }
    }

}
