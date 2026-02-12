package ru.clouddonate.cloudpayments.cart.database;

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.clouddonate.cloudpayments.cart.CartService;
import ru.clouddonate.cloudpayments.cart.model.PlayerCart;
import ru.clouddonate.cloudpayments.cart.model.Product;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

@RequiredArgsConstructor
public class DataBase {

    private final Plugin plugin;
    private final CartService cartService;

    private final String tableName = "carts";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Connection connection;

    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver not found", e);
        }
        executor.submit(() -> {
            try {
                String url = plugin.getDataFolder().getAbsolutePath() + File.separator + "cartStorage.db";
                connection = DriverManager.getConnection("jdbc:sqlite:" + url);
                plugin.getLogger().info("База данных успешно подключена");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось подключиться к базе данных из-за непредвиденной ошибки", e);
                return;
            }
            String createSql = "CREATE TABLE IF NOT EXISTS " + tableName + " (playerName TEXT PRIMARY KEY UNIQUE NOT NULL, products TEXT NOT NULL);";
            try (PreparedStatement stat = connection.prepareStatement(createSql)) {
                stat.execute();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Не удалось создать таблицу в базе данных");
            }
        });
    }

    public void disconnect() {
        Future<?> future = executor.submit(() -> {
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка закрытия соединения", e);
            }
        });
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            plugin.getLogger().log(Level.SEVERE, "Ошибка при завершении работы базы данных: ", e);
        } finally {
            executor.shutdown();
        }
    }

    public @NotNull Future<@Nullable PlayerCart> get(@NotNull String playerName) {
        return executor.submit(() -> {
            String sql = "SELECT products FROM " + tableName + " WHERE playerName = ?;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, playerName);

                try (ResultSet set = ps.executeQuery()) {
                    if (!set.next()) return null;

                    List<Product> products = PlayerCart.deserializeProducts(set.getString("products"));
                    return new PlayerCart(cartService, playerName, products);
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка работы базы данных get", e);
                return null;
            }
        });
    }

    public @NotNull Future<@NotNull List<@NotNull PlayerCart>> getAll() {
        return executor.submit(() -> {
            List<PlayerCart> result = new ArrayList<>();

            String sql = "SELECT * FROM " + tableName + ";";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                try (ResultSet set = ps.executeQuery()) {
                    while (set.next()) {
                        String playerName = set.getString("playerName");
                        List<Product> products = PlayerCart.deserializeProducts(set.getString("products"));
                        PlayerCart data = new PlayerCart(cartService, playerName, products);
                        result.add(data);
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка работы базы данных getAll", e);
            }
            return result;
        });
    }

    public void save(@NotNull PlayerCart object) {
        executor.submit(() -> {
            String sql = "INSERT INTO " + tableName + " (playerName, products) "
                    + "VALUES (?, ?) "
                    + "ON CONFLICT(playerName) DO UPDATE SET products = excluded.products;";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, object.getPlayerName());
                ps.setString(2, PlayerCart.serializeProducts(object.getProducts()));
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Ошибка работы базы данных save", e);
            }
        });
    }


}
