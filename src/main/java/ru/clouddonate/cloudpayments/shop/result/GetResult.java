package ru.clouddonate.cloudpayments.shop.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetResult {

    private final int id;
    private final int product_id;
    private final String name;
    private final int price;
    private final int amount;
    private final String nickname;
    private final String[] commands;

}
