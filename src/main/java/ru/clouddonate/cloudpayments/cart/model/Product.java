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

    private final int id;
    private final String name;
    private final int price;
    private final int amount;
    private final List<String> commands;

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Product object = (Product) obj;
        return id == object.getId() && price == object.getPrice()
                && amount == object.getAmount();
    }

    public @NotNull String serialize() {
        StringBuilder sb = new StringBuilder();
        //sb.append("{").append(id).append(",").append(price).append(",").append(amount).append(",").append("{");
        sb.append("{id=").append(id).append(",name=").append(name).append(",price=").append(price).append(",amount=").append(amount).append(",commands=").append("{");
        boolean first = true;
        for (String cmd : commands) {
            if (!first) sb.append(",");
            else first = false;
            sb.append(cmd);
        }
        sb.append("}}");
        return sb.toString();
    }

    public static @Nullable Product deserialize(@NotNull String value) throws Exception {
        if (!value.startsWith("{") || !value.endsWith("}")) return null;
        String content = value.substring(1, value.length() - 1);

        //int commandsStart = content.indexOf(",{");
        //if (commandsStart == -1 || !content.endsWith("}")) return null;

        int indexId = content.indexOf("id=");
        int indexName = content.indexOf("name=");
        int indexPrice = content.indexOf("price=");
        int indexAmount = content.indexOf("amount=");
        int indexCommands = content.indexOf("commands=");

        int id = 0;
        String name = "";
        int price = 0;
        int amount = 1;
        List<String> commands = new ArrayList<>();

        if (indexId != -1) {
            int endId = content.indexOf(',', indexId);
            id = Integer.parseInt(content.substring(indexId + 3, endId));
        }
        if (indexName != -1) {
            int endName = content.indexOf(',', indexName);
            name = content.substring(indexName + 5, endName);
        }
        if (indexPrice != -1) {
            int endPrice = content.indexOf(',', indexPrice);
            price = Integer.parseInt(content.substring(indexPrice + 6, endPrice));
        }
        if (indexAmount != -1) {
            int endAmount = content.indexOf(',', indexAmount);
            amount = Integer.parseInt(content.substring(indexAmount + 7, endAmount));
        }
        if (indexCommands != -1) {
            int endCommands = content.indexOf("}", indexCommands);
            String commandsContent = content.substring(indexCommands + 10, endCommands);
            commands.addAll(Arrays.asList(commandsContent.split(",")));
        }

        return new Product(id, name, price, amount, commands);
    }

}
