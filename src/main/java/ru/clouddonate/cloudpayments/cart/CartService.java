package ru.clouddonate.cloudpayments.cart;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.clouddonate.cloudpayments.cart.database.DataBase;
import ru.clouddonate.cloudpayments.cart.listener.CartListener;
import ru.clouddonate.cloudpayments.cart.model.PlayerCart;
import ru.clouddonate.cloudpayments.cart.model.Product;
import ru.clouddonate.cloudpayments.configuration.ConfigurationService;
import ru.clouddonate.cloudpayments.service.Service;

import java.util.List;

public class CartService implements Service {

    private final DataBase dataBase;
    private final CartListener cartListener;

    public CartService(Plugin plugin, ConfigurationService configurationService) {
        dataBase = new DataBase(plugin, this);
        cartListener = new CartListener(plugin, this, configurationService.getMessagesFile());
    }

    @Override
    public void enable() {
        dataBase.connect();
        cartListener.register();
    }

    @Override
    public void disable() {
        cartListener.unregister();
        dataBase.disconnect();
    }

    public void save(@NotNull PlayerCart playerData) {
        dataBase.save(playerData);
    }

    public void addToCart(@NotNull String playerName, @NotNull String productId, int price,
                          int amount, @NotNull List<String> commands) {
        PlayerCart cart = getCart(playerName);
        if (cart == null) cart = new PlayerCart(this, playerName);
        cart.getProducts().add(new Product(productId, price, amount, commands));
        save(cart);
    }

    public @Nullable PlayerCart getCart(@NotNull String playerName) {
        try {
            return dataBase.get(playerName).get();
        } catch (Exception e) {
            return null;
        }
    }

    public @NotNull PlayerCart getCartOrGenerate(@NotNull String playerName) {
        PlayerCart cart = getCart(playerName);
        if (cart == null) cart = new PlayerCart(this, playerName);
        return cart;
    }

    public boolean hasProductsInCart(@NotNull String playerName) {
        PlayerCart cart = getCart(playerName);
        return cart != null && !cart.getProducts().isEmpty();
    }

}
