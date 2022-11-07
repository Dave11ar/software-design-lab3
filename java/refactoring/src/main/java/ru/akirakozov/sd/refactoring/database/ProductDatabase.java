package ru.akirakozov.sd.refactoring.database;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

public class ProductDatabase {
    private final String connectionURL;

    public ProductDatabase(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    public void addProduct(String name, long price) throws SQLException {
        update("INSERT INTO PRODUCT " + "(NAME, PRICE) VALUES (\"" +
            name + "\"," + String.valueOf(price) + ")");
    }

    public List<Map.Entry<String, Long>> getProducts() throws SQLException {
        return select("SELECT * FROM PRODUCT", rs -> {
            List<Map.Entry<String, Long>> resultList = new ArrayList<>();

            try {
                while (rs.next()) {
                    String name = rs.getString("name");
                    long price = rs.getLong("price");
                    resultList.add(new AbstractMap.SimpleImmutableEntry<>(name, price));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            return resultList;
        });
    }

    public Map.Entry<String, Long> calc(String query) throws SQLException {
        String sql = "";

        switch (query) {
            case "max":
                sql = "SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1";
                break;
            case "min":
                sql = "SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1";
                break;
            case "sum":
                sql = "SELECT SUM(price) FROM PRODUCT";
                break;
            case "count":
                sql = "SELECT COUNT(*) FROM PRODUCT";
                break;
        }

        return select(sql, rs -> {
            try {
                if (rs.next()) {
                    switch (query) {
                        case "max":
                        case "min":
                            String name = rs.getString("name");
                            long price = rs.getLong("price");
                            return new AbstractMap.SimpleImmutableEntry<>(name, price);
                        default:
                            return new AbstractMap.SimpleImmutableEntry<>("", rs.getLong(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public void initDB() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " NAME           TEXT    NOT NULL, " +
                " PRICE          INT     NOT NULL)";
        update(query);
    }

    public void cleanDB() throws SQLException {
        String query = "DELETE FROM PRODUCT WHERE 1 = 1";
        update(query);
    }

    private void update(String sql) throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionURL)) {
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    private <T> T select(String sql, Function<ResultSet, T> processResultSet) throws SQLException {
        try (Connection c = DriverManager.getConnection(connectionURL)) {
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            T result = processResultSet.apply(rs);

            rs.close();
            stmt.close();

            return result;
        }
    }
}
