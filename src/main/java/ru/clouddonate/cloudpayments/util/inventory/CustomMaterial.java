package ru.clouddonate.cloudpayments.util.inventory;

import lombok.RequiredArgsConstructor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.basher.configuration.CommentConfigurationSection;

public abstract class CustomMaterial {

    public abstract @NotNull ItemStack getItemStack();
    public abstract @NotNull Material getType();

    @NotNull
    public static CustomMaterial of(@NotNull CommentConfigurationSection section) {
        String value = section.getString("material", "");
        Material material = Material.getMaterial(value.toUpperCase());
        if (material == null) material = Material.STONE;

        if(material.name().contains("POTION") || material == Material.TIPPED_ARROW) {
            Color color = getColor(section);
            return new Potion(material, color);
        } else if(material.name().contains("LEATHER_")) {
            Color color = getColor(section);
            return new LeatherArmor(material, color);
        } else {
            return new Default(material);
        }
    }

    private static @Nullable Color getColor(@NotNull CommentConfigurationSection section) {
        Color color = null;
        String rgb = section.getString("color");
        if (rgb != null) {
            try {
                String[] arr = rgb.split(";");
                color = Color.fromRGB(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer.parseInt(arr[2]));
            } catch (Exception ignored) {
            }
        }
        return color;
    }

    @RequiredArgsConstructor
    public static class Default extends CustomMaterial {
        private final Material material;

        @Override
        public @NotNull ItemStack getItemStack() {
            return new ItemStack(material, 1);
        }

        @Override
        public @NotNull Material getType() {
            return material;
        }
    }

    @RequiredArgsConstructor
    public static class Potion extends CustomMaterial {
        private final Material material;
        @Nullable
        private final Color color;

        @Override
        public @NotNull ItemStack getItemStack() {
            ItemStack itemStack = new ItemStack(material);
            if (color != null) {
                PotionMeta meta = (PotionMeta)itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            }

            return itemStack;
        }

        @Override
        public @NotNull Material getType() {
            return material;
        }
    }

    @RequiredArgsConstructor
    public static class LeatherArmor extends CustomMaterial {
        private final Material material;
        @Nullable
        private final Color color;

        @Override
        public @NotNull ItemStack getItemStack() {
            ItemStack itemStack = new ItemStack(material);
            if(color != null) {
                LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            }
            return itemStack;
        }

        @Override
        public @NotNull Material getType() {
            return material;
        }
    }
}
