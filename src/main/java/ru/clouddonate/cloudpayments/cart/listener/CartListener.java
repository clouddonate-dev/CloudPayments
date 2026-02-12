package ru.clouddonate.cloudpayments.cart.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import ru.clouddonate.cloudpayments.cart.CartService;
import ru.clouddonate.cloudpayments.configuration.MessagesFile;

@RequiredArgsConstructor
public class CartListener implements Listener {

    private final Plugin plugin;
    private final CartService cartService;
    private final MessagesFile messagesFile;

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        PlayerJoinEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if(cartService.hasProductsInCart(player.getName())) {
            for(String msg : messagesFile.getCartNotify()) player.sendMessage(msg);
        }
    }

}
