package ru.clouddonate.cloudpayments.shop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShopSettings {

    private final String shopId;
    private final String shopKey;
    private final String serverId;

}
