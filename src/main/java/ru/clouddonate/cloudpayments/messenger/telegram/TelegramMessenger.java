package ru.clouddonate.cloudpayments.messenger.telegram;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.clouddonate.cloudpayments.messenger.Messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class TelegramMessenger implements Messenger {

    private final Logger logger;

    private final String apiToken;
    private final List<String> ids = new ArrayList<>();

    public TelegramMessenger(Logger logger, String apiToken, List<String> ids) {
        this.logger = logger;
        this.apiToken = apiToken;
        this.ids.addAll(ids);
    }

    @Override
    public void connect() throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + this.apiToken + "/getMe");
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("Invalid bot token or unable to connect to Telegram API. Response code: " + responseCode);
            }
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            if (!response.toString().contains("\"ok\":true")) {
                throw new IOException("Telegram API response indicates failure: " + response);
            }
            connection.disconnect();
        } catch (IOException e) {
            throw new IOException("Failed to connect to Telegram API: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void sendMessage(@NotNull String message) {
        ids.forEach(id -> {
            try {
                URL url = new URL("https://api.telegram.org/bot" + this.getApiToken() + "/sendMessage");
                HttpURLConnection connection = TelegramMessenger.getHttpURLConnection(message, id, url);
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    logger.warning("Failed to send message to Telegram. Response Code: " + responseCode);
                }
                connection.disconnect();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to send message to Telegram", e);
            }
        });
    }

    private static HttpURLConnection getHttpURLConnection(String message, String id, URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        String payload = "{\"chat_id\":\"" + id + "\",\"text\":\"" + message + "\"}";
        try (OutputStream os = connection.getOutputStream();) {
            byte[] input = payload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return connection;
    }

}
