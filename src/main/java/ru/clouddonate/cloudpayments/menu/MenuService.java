package ru.clouddonate.cloudpayments.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.clouddonate.cloudpayments.menu.listener.InventoryListener;
import ru.clouddonate.cloudpayments.service.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MenuService implements Service {

    private static final long CLICK_COOLDOWN = 250L;
    private static Plugin plugin;

    private final InventoryListener inventoryListener;

    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public MenuService(Plugin plugin) {
        MenuService.plugin = plugin;
        inventoryListener = new InventoryListener(plugin, this);
    }

    @Override
    public void enable() {
        inventoryListener.register();
    }

    @Override
    public void disable() {
        inventoryListener.unregister();
        cooldowns.clear();
    }

    public void removeCooldown(@NotNull UUID playerUniqueId) {
        cooldowns.remove(playerUniqueId);
    }

    public boolean canClick(@NotNull UUID playerUniqueId) {
        long now = System.currentTimeMillis();
        Long inMap = cooldowns.get(playerUniqueId);

        if(inMap != null && inMap + CLICK_COOLDOWN > now) return false;

        cooldowns.put(playerUniqueId, now);
        return true;
    }

    public static void openMenuAsync(@NotNull Player player, @NotNull Supplier<Menu> constructor) {
        CompletableFuture.supplyAsync(()-> {
            try {
                return constructor.get();
            } catch (Exception e) {
                plugin.getLogger().warning(e.getMessage());
                return null;
            }
        }).thenAccept(menu -> {
            if(menu == null) return;
            Bukkit.getScheduler().runTask(plugin, () -> menu.open(player));
        });
    }

}
