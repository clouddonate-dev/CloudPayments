package ru.clouddonate.cloudpayments.messenger;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface Messenger {

    void connect() throws IOException;
    void disconnect();

    void sendMessage(@NotNull String message);

}
