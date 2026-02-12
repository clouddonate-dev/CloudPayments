package ru.clouddonate.cloudpayments.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.clouddonate.cloudpayments.shop.result.GetResult;

@Getter
@Setter
public final class PurchaseApproveEvent extends Event {
    public static HandlerList handlerList = new HandlerList();
    private final GetResult result;

    public PurchaseApproveEvent(GetResult result) {
        super(true);
        this.result = result;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
