package ru.gb.alex.cloud.server.services;

import java.sql.*;

public class SQLiteAuthService implements AuthService {
    private static Connection connection;
    private static Statement statement;

    @Override
    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            statement = connection.createStatement();
            createTable();
        } catch (SQLException sq) {
            sq.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean authentication(String username, String password) {
        try (ResultSet rsRead = statement.executeQuery("SELECT " +
                "username, password FROM accounts")) {
            while (rsRead.next()) {
                if (rsRead.getString("username").equals(username) &&
                rsRead.getString("password").equals(password)) {
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static void createTable() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                " username TEXT NOT NULL UNIQUE,\n" +
                " password TEXT NOT NULL\n" +
                " );");
    }

    @Override
    public boolean createNewAccount(String username, String password) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO accounts (username, password) " +
                        "VALUES (?, ?)")){
            ps.setString(1, username);
            ps.setString(2, password);
            ps.execute();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void changePassword(String username, String newPassword) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE accounts SET password = ? WHERE username = ?;")) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
