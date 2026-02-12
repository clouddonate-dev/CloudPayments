package ru.clouddonate.cloudpayments.cart.model;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.clouddonate.cloudpayments.cart.CartService;

import java.util.*;

@Getter
public class PlayerCart {

    @Getter(AccessLevel.PRIVATE)
    private final CartService service;

    private final String playerName;
    private final List<Product> products;

    public PlayerCart(CartService service, String playerName) {
        this(service, playerName, new ArrayList<>());
    }

    public PlayerCart(CartService service, String playerName, List<Product> products) {
        this.service = service;
        this.playerName = playerName;
        this.products = products;
    }

    public void save() {
        service.save(this);
    }

    public @Nullable Player asBukkitPlayer() {
        return Bukkit.getPlayer(playerName);
    }

    @Override
    public @NotNull String toString() {
        return "PlayerCart{playerName=" + playerName + ", products=" + serializeProducts(products) + "}";
    }

    @Override
    public int hashCode() {
        return playerName.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        PlayerCart object = (PlayerCart) obj;
        return playerName.equals(object.getPlayerName());
    }

    public static @NotNull String serializeProducts(@NotNull List<Product> products) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Product product : products) {
            if(!first) sb.append(";");
            else first = false;
            sb.append(product.serialize());
        }
        return sb.toString();
    }

    public static @NotNull List<Product> deserializeProducts(@NotNull String value) {
        String[] split = value.split(";");
        List<Product> products = new ArrayList<>();
        for(String product : split) {
            products.add(Product.deserialize(product));
        }
        return products;
    }

}
