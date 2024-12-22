package ru.gb.cloud.server;

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

//    /**
//     * Получить никнейм по логину/паролю.
//     * @param username
//     * @param password
//     * @return никнейм, если найден, или null, если такого нет
//     */
//    String getNickByLoginAndPass(String username, String password);

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

    boolean authentification(String username, String password);
}
