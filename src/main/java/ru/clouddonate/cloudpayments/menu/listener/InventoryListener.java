package ru.clouddonate.cloudpayments.menu.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import ru.clouddonate.cloudpayments.menu.Menu;
import ru.clouddonate.cloudpayments.menu.MenuService;

@RequiredArgsConstructor
public class InventoryListener implements Listener {

    private final Plugin plugin;
    private final MenuService service;

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if(event.getSlot() < 0) return;

        Player player = (Player)event.getWhoClicked();
        Inventory inv = player.getOpenInventory().getTopInventory();

        if(inv.getHolder() instanceof Menu) {
            if(service.canClick(player.getUniqueId())) {
                ((Menu)inv.getHolder()).onInventoryClick(event);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        service.removeCooldown(event.getPlayer().getUniqueId());
        InventoryHolder holder = event.getInventory().getHolder();
        if(event.getInventory().getHolder() instanceof Menu) {
            ((Menu)holder).onInventoryClose(event);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.removeCooldown(event.getPlayer().getUniqueId());
    }

}
