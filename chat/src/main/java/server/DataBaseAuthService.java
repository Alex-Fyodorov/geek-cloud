package server;

import java.sql.*;

public class DataBaseAuthService implements AuthService {
    private static Connection connection;
    private static Statement statement;
    //private static ResultSet rsRead;

    /**
     * В этом методе помимо всего прочего хотел открыть переменную rsRead,
     * (она используется в методе getNickByLoginAndPass), как об этом и
     * говорили на лекции. Ведь если создавать аккаунты или менять ник
     * пользователи будут нечасто, то заходить новые - постоянно.
     * Поэтому смысла её каждый раз закрывать большого нет. Однако у меня
     * не получилось. Инициализацию, открытую в методе start, метод
     * getNickByLoginAndPass не видит. Я не стал всё удалять, просто
     * закомментировал. Надеюсь на твоё объяснение.
     */
    @Override
    public void start() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:accounts.db");
            statement = connection.createStatement();
            /*rsRead = statement.executeQuery("SELECT " +
                    "login, password, nick FROM accounts");*/
            createTable();
            //insertAccountsBatch(); //Этот метод используется только в самый
            // первый раз, поэтому он закомментирован.
        } catch (SQLException sq) {
            sq.printStackTrace();
        }
    }

    @Override
    public void stop() {
        /*try {
            if (rsRead != null) {
                rsRead.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

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
    public String getNickByLoginAndPass(String login, String pass) {
        try (ResultSet rsRead = statement.executeQuery("SELECT " +
                "login, password, nick FROM accounts")) {
            while (rsRead.next()) {
                if (rsRead.getString("login").equals(login) &&
                rsRead.getString("password").equals(pass)) {
                    return rsRead.getString("nick");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void createTable() throws SQLException {
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (\n" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
                " login TEXT NOT NULL UNIQUE,\n" +
                " password TEXT NOT NULL,\n" +
                " nick TEXT NOT NULL UNIQUE\n" +
                " );");
    }

    /**
     * Этот метод можно было сделать проще, сослаться в нём на метод
     * newAccount. Сделал так просто для закрепления материала.
     */
    private static void insertAccountsBatch() {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO accounts (login, password, nick) " +
                        "VALUES (?, ?, ?)")) {
            for (int i = 1; i <= 3; i++) {
                ps.setString(1, "l" + i);
                ps.setString(2, "p" + i);
                ps.setString(3, "nick" + i);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean newAccount(String login, String pass, String nick) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO accounts (login, password, nick) " +
                        "VALUES (?, ?, ?)")){
            ps.setString(1, login);
            ps.setString(2, pass);
            ps.setString(3, nick);
            ps.execute();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public void changeNick(String oldNick, String newNick) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE accounts SET nick = ? WHERE nick = '" +
                        oldNick + "';")) {
            ps.setString(1, newNick);
            ps.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
