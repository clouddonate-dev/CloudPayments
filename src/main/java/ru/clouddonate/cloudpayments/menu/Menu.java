package ru.clouddonate.cloudpayments.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public interface Menu extends InventoryHolder {

    default void onInventoryOpen(@NotNull Player player) {}

    default void onInventoryClick(@NotNull InventoryClickEvent event) {}

    default void onInventoryClose(@NotNull InventoryCloseEvent event) {}

    default void open(@NotNull Player player) {
        onInventoryOpen(player);
        player.openInventory(getInventory());
    }

}
