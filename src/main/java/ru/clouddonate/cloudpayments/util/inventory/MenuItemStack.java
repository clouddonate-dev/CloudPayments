package ru.clouddonate.cloudpayments.util.inventory;

import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MenuItemStack {

    private final ItemStack itemStack;
    private final List<Integer> slots = new ArrayList<>();

    public MenuItemStack() {
        itemStack = new ItemStackGenerator().generate();
    }

    public MenuItemStack(@NotNull CommentConfigurationSection section) {
        itemStack = new ItemStackGenerator(section).generate();
        List<Integer> slots = section.getList("slots", Integer.class);
        if (slots != null) {
            this.slots.addAll(slots);
        } else {
            this.slots.add(section.getInt("slot", 0));
        }
    }

    public @NotNull ItemStack getItemStackCopy() {
        return itemStack.clone();
    }

    public int getSlot() {
        return slots.get(0);
    }

    public boolean hasSlot(int slot) {
        return slots.contains(slot);
    }

    public void copyToInv(@NotNull Inventory inventory) {
        ItemStack copy = getItemStackCopy();
        for (int slot : slots) {
            inventory.setItem(slot, copy);
        }
    }

}
