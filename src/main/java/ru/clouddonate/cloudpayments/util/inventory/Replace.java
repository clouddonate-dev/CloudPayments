package ru.clouddonate.cloudpayments.util.inventory;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Replace {

    private final String oldValue;
    private final List<String> newValue = new ArrayList<>();

    public Replace(@NotNull String oldValue, @NotNull String newValue) {
        this.oldValue = oldValue;
        this.newValue.add(newValue);
    }

    public Replace(@NotNull String oldValue, @NotNull List<String> newValue) {
        this.oldValue = oldValue;
        this.newValue.addAll(newValue);
    }

    public void apply(@NotNull List<String> target) {
        if(newValue.isEmpty()) {
            for(int i = 0; i < target.size(); ++i) {
                String str = target.get(i);
                if(str.contains(oldValue)){
                    target.remove(i);
                    i--;
                }
            }
            return;
        }

        if(newValue.size() == 1) {
            String value = newValue.get(0);
            target.replaceAll(s -> s.replace(oldValue, value));
        } else {
            for(int i = 0; i < target.size(); ++i) {
                String targetStr = target.get(i);
                if(targetStr.contains(oldValue)) {
                    int toRemove = i;
                    for(String newStr : newValue) {
                        target.add(i + 1, newStr);
                        ++i;
                    }
                    target.remove(toRemove);
                }
            }
        }
    }

    public @NotNull String apply(@NotNull String str) {
        if(newValue.isEmpty()) return str;
        return str.replace(oldValue, newValue.get(0));
    }
}
