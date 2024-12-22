package server;

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
     * Получить никнейм по логину/паролю.
     * @param login
     * @param pass
     * @return никнейм, если найден, или null, если такого нет
     */
    String getNickByLoginAndPass(String login, String pass);

    /**
     * Изменить nick.
     * @param oldNick
     * @param newNick
     */
    void changeNick(String oldNick, String newNick);

    /**
     * Создание нового аккаунта.
     * @param login
     * @param pass
     * @param nick
     * @return true, если успешно.
     */
    boolean newAccount(String login, String pass, String nick);
}
