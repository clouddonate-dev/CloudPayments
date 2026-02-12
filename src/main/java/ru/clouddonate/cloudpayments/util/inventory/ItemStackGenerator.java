package ru.clouddonate.cloudpayments.util.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import ru.basher.configuration.CommentConfigurationSection;
import ru.clouddonate.cloudpayments.util.TextUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Getter
@Setter
@AllArgsConstructor
public class ItemStackGenerator {

    private String name;
    private List<String> lore = new ArrayList<>();
    private CustomMaterial material;
    private int amount;
    private Map<Enchantment, Integer> enchants = new HashMap<>();
    private List<ItemFlag> itemFlags = new ArrayList<>();

    public ItemStackGenerator() {
        this("", new ArrayList<>(), new CustomMaterial.Default(Material.STONE), 1, new HashMap<>(), new ArrayList<>());
    }

    public ItemStackGenerator(@NotNull CommentConfigurationSection section) {
        name = TextUtil.toColor(section.getString("name"));

        for(String s : section.getStringList("lore")) {
            if (s != null) {
                lore.add(TextUtil.toColor(s));
            }
        }

        material = CustomMaterial.of(section);
        amount = section.getInt("amount", 1);

        for(String s : section.getStringList("enchants")) {
            if (s != null) {
                String[] arr = s.split(";");
                if (arr.length == 2) {
                    Enchantment enchantment = Enchantment.getByName(arr[0]);
                    if (enchantment != null) {
                        int level = Integer.parseInt(arr[1]);
                        enchants.put(enchantment, level);
                    }
                }
            }
        }

        for(String s : section.getStringList("itemFlags")) {
            if (s != null) {
                ItemFlag itemFlag = ItemFlag.valueOf(s.toUpperCase());
                itemFlags.add(itemFlag);
            }
        }
    }

    public @NotNull ItemStack generate(@NotNull Replace... replaces) {
        return generateArr(replaces);
    }

    public @NotNull ItemStack generateArr(@NotNull Replace[] replaces) {
        ItemStack itemStack = material.getItemStack();
        itemStack.setAmount(amount);
        ItemMeta meta = itemStack.getItemMeta();
        String name = this.name;
        List<String> lore = new ArrayList<>(this.lore);
        if (replaces != null) {
            for (Replace replace : replaces) {
                name = replace.apply(name);
                replace.apply(lore);
            }
        }

        meta.setDisplayName(name);
        meta.setLore(lore);

        for (Entry<Enchantment, Integer> entry : this.enchants.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }

        for (ItemFlag itemFlag : this.itemFlags) {
            meta.addItemFlags(itemFlag);
        }

        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public void combine(@NotNull ItemStackGenerator child, boolean isChildPriority) {
        if (this.name.isEmpty()) {
            this.name = child.getName();
        } else {
            this.name = this.name.replace("{name}", child.getName());
        }

        if (this.lore.isEmpty()) {
            this.lore.addAll(child.getLore());
        } else {
            new Replace("{lore}", child.getLore()).apply(this.lore);
        }

        this.material = isChildPriority ? child.getMaterial() : this.material;

        this.amount = isChildPriority ? child.getAmount() : this.amount;

        for (Entry<Enchantment, Integer> entry : child.getEnchants().entrySet()) {
            this.enchants.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        for (ItemFlag itemFlag : child.getItemFlags()) {
            if (!this.itemFlags.contains(itemFlag)) {
                this.itemFlags.add(itemFlag);
            }
        }
    }

    @NotNull
    public ItemStackGenerator copy() {
        return new ItemStackGenerator(
                this.name,
                new ArrayList<>(this.lore),
                this.material,
                this.amount,
                new HashMap<>(this.enchants),
                new ArrayList<>(this.itemFlags)
        );
    }
}
