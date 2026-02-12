package ru.clouddonate.cloudpayments.configuration;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.clouddonate.cloudpayments.configuration.migrate.MigratableFile;
import ru.clouddonate.cloudpayments.configuration.migrate.MigrationIO;
import ru.clouddonate.cloudpayments.util.TextUtil;
import ru.clouddonate.cloudpayments.util.inventory.ItemStackGenerator;
import ru.clouddonate.cloudpayments.util.inventory.MenuItem;
import ru.clouddonate.cloudpayments.util.inventory.MenuItemStack;

import java.util.*;

@Getter
public class CartFile implements MigratableFile {

    private boolean enabled;
    private boolean onlyForOffline;

    private String menuName;
    private int menuSize;
    private final List<Integer> menuSlots = new ArrayList<>();

    private ItemStackGenerator menuProductItem;
    private MenuItem menuPageItem;
    private MenuItemStack menuPagePrevItem;
    private MenuItemStack menuPageNextItem;

    private final Map<Integer, ItemStack> menuDecor = new HashMap<>();

    @Override
    public @NotNull String fileName() {
        return "cart.yml";
    }

    @Override
    public void load(@NotNull CommentConfigurationSection config) {
        enabled = config.getBoolean("enabled", false);
        onlyForOffline = config.getBoolean("onlyForOffline", false);

        menuName = TextUtil.toColor(config.getString("menu.name"));
        menuSize = config.getInt("menu.size", 54);

        menuSlots.clear();
        menuSlots.addAll(config.getIntegerList("menu.slots"));

        CommentConfigurationSection menuProductItemSec = config.getConfigurationSection("menu.productItem");
        if (menuProductItemSec == null) menuProductItem = new ItemStackGenerator();
        else menuProductItem = new ItemStackGenerator(menuProductItemSec);

        CommentConfigurationSection menuPageItemSec = config.getConfigurationSection("menu.pageItem");
        if (menuPageItemSec == null) menuPageItem = new MenuItem();
        else menuPageItem = new MenuItem(menuPageItemSec);

        CommentConfigurationSection menuPagePrevItemSec = config.getConfigurationSection("menu.pagePrevItem");
        if (menuPagePrevItemSec == null) menuPagePrevItem = new MenuItemStack();
        else menuPagePrevItem = new MenuItemStack(menuPagePrevItemSec);

        CommentConfigurationSection menuPageNextItemSec = config.getConfigurationSection("menu.pageNextItem");
        if (menuPageNextItemSec == null) menuPageNextItem = new MenuItemStack();
        else menuPageNextItem = new MenuItemStack(menuPageNextItemSec);

        menuDecor.clear();
        CommentConfigurationSection menuDecorSec = config.getConfigurationSection("menu.decor");
        if (menuDecorSec != null) {
            for(String key : menuDecorSec.getMap().keySet()) {
                CommentConfigurationSection keySec = menuDecorSec.getConfigurationSection(key);
                if(keySec == null) continue;
                MenuItemStack menuItemStack = new MenuItemStack(keySec);
                for (int slot : menuItemStack.getSlots()) menuDecor.put(slot, menuItemStack.getItemStackCopy());
            }
        }
    }

    @Override
    public void migrate(@NotNull MigrationIO io, int fromVersion) throws Exception {
    }


}
