package ru.clouddonate.cloudpayments.util.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.attribute.Attribute;

@RequiredArgsConstructor
@Getter
public class ItemAttribute {

    private final Attribute attribute;
    private final double value;
    private final boolean isPercent;

}
