package ru.clouddonate.cloudpayments.util;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonUtil {

    public static final Gson GSON = new Gson();

    public static @NotNull String serialize(@NotNull Object object) {
        return GSON.toJson(object);
    }


    public static @Nullable <T> T deserialize(@NotNull String value, @NotNull Class<T> clazz) {
        return GSON.fromJson(value, clazz);
    }

}
