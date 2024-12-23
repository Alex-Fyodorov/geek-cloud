package ru.gb.cloud.server.databases;

/**
 * Сервис аутентификации.
 */
public interface AuthService {

    /**
     * Запустить сервис.
     */
    void start();

    /**
     * Отключить сервис.
     */
    void stop();

    /**
     * Аутентификация пользователя.
     * @param username
     * @param password
     * @return
     */
    boolean authentification(String username, String password);

    /**
     * Изменить пароль.
     * @param username
     * @param newPassword
     */
    void changePassword(String username, String newPassword);

    /**
     * Создание нового аккаунта.
     * @param username
     * @param password
     * @return true, если успешно.
     */
    boolean createNewAccount(String username, String password);
}
