package ru.clouddonate.cloudpayments.cart.menu;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.clouddonate.cloudpayments.announcement.AnnouncementService;
import ru.clouddonate.cloudpayments.cart.model.PlayerCart;
import ru.clouddonate.cloudpayments.cart.model.Product;
import ru.clouddonate.cloudpayments.configuration.CartFile;
import ru.clouddonate.cloudpayments.configuration.ConfigFile;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.menu.Menu;
import ru.clouddonate.cloudpayments.util.inventory.Replace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CartMenu implements Menu {

    private final AnnouncementService announcementService;

    private final CartFile menuFile;
    private final ConfigFile configFile;

    private final PlayerCart cart;

    private final Map<Integer, Product> productSlots = new ConcurrentHashMap<>();

    private int page = 1;
    private boolean hasNextPage = false;

    @Getter
    private final Inventory inventory;

    public CartMenu(ConfigurationService configurationService, AnnouncementService announcementService, PlayerCart cart) {
        this.announcementService = announcementService;

        this.menuFile = configurationService.getCartFile();
        this.configFile = configurationService.getConfigFile();

        this.cart = cart;

        this.inventory = Bukkit.createInventory(this, menuFile.getMenuSize(), menuFile.getMenuName());

        for (Map.Entry<Integer, ItemStack> entry : menuFile.getMenuDecor().entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue().clone());
        }

        menuFile.getMenuPagePrevItem().copyToInv(inventory);
        menuFile.getMenuPageNextItem().copyToInv(inventory);

        updatePage();
    }

    private void updatePage() {
        productSlots.clear();
        for (int slot : menuFile.getMenuSlots()) inventory.clear(slot);

        List<Product> products = cart.getProducts();

        int countPerPage = menuFile.getMenuSlots().size();
        int startId = (page - 1) * countPerPage;

        while (startId >= products.size()) {
            if (page == 1) break;
            page--;
            startId = (page - 1) * countPerPage;
        }

        if (!products.isEmpty()) {
            int id = startId;
            for (int i = 0; i < countPerPage; i++) {
                if (products.size() <= id) break;

                int slot = menuFile.getMenuSlots().get(i);
                Product product = products.get(id);
                inventory.setItem(slot, menuFile.getMenuProductItem().generate(
                        new Replace("{product}", product.getId())
                ));
                productSlots.put(slot, product);

                id++;
            }
        }

        hasNextPage = products.size() > page * countPerPage;
        menuFile.getMenuPageItem().copyToInv(inventory,
                new Replace("{page}", page + "")
        );
    }

    @Override
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory() != this.inventory) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (menuFile.getMenuPagePrevItem().hasSlot(slot)) {
            if (page < 2) return;
            page--;
            updatePage();
        } else if (menuFile.getMenuPageNextItem().hasSlot(slot)) {
            if (hasNextPage) {
                page++;
                updatePage();
            }
        } else {
            Product product = productSlots.get(slot);
            if (product != null && removeProduct(cart.getProducts(), product)) {
                cart.save();
                updatePage();
                ConsoleCommandSender console = Bukkit.getConsoleSender();
                for (String command : product.getCommands()) {
                    if (configFile.isSettingsDebugMode()) Bukkit.getLogger().info("Executing command: " + command);
                    Bukkit.dispatchCommand(console, command
                            .replace("{user}", player.getName())
                            .replace("{amount}", product.getAmount() + ""));
                }
                announcementService.handle(product.getId(), player.getName(), product.getPrice(), product.getAmount());
            }
        }
    }

    private boolean removeProduct(@NotNull List<Product> products, @NotNull Product product) {
        for(int i = 0; i < products.size(); i++) {
            Product prod = products.get(i);
            if(prod.equals(product)) {
                products.remove(i);
                return true;
            }
        }
        return false;
    }
}