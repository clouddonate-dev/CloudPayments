package ru.clouddonate.cloudpayments.cart.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class Product {

    private final String id;
    private final int price;
    private final int amount;
    private final List<String> commands;

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Product object = (Product) obj;
        return id.equals(object.getId()) && price == object.getPrice()
                && amount == object.getAmount();
    }

    public @NotNull String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("{").append(id).append(",").append(price).append(",").append(amount).append(",").append("{");
        boolean first = true;
        for(String cmd : commands) {
            if(!first) sb.append(",");
            else first = false;
            sb.append(cmd);
        }
        sb.append("}}");
        return sb.toString();
    }

    public static @NotNull Product deserialize(@NotNull String value) {
        if (!value.startsWith("{") || !value.endsWith("}")) {
            throw new IllegalArgumentException("Invalid product format: " + value);
        }
        String content = value.substring(1, value.length() - 1);

        int commandsStart = content.indexOf(",{");
        if (commandsStart == -1 || !content.endsWith("}")) {
            throw new IllegalArgumentException("Invalid product format: " + value);
        }

        String[] mainParts = content.substring(0, commandsStart).split(",", 3);
        if (mainParts.length != 3) {
            throw new IllegalArgumentException("Invalid product format: " + value);
        }

        String id = mainParts[0];
        int price = Integer.parseInt(mainParts[1]);
        int amount = Integer.parseInt(mainParts[2]);

        String commandsPart = content.substring(commandsStart + 2, content.length() - 1);
        List<String> commands;

        if (commandsPart.isEmpty()) {
            commands = new ArrayList<>();
        } else {
            commands = new ArrayList<>(Arrays.asList(commandsPart.split(",")));
        }

        return new Product(id, price, amount, commands);
    }

}
