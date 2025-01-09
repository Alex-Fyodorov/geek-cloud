package ru.gb.alex.cloud.server.services;

import jakarta.persistence.PersistenceException;
import org.hibernate.Session;
import ru.gb.alex.cloud.server.utils.SessionFactoryUtils;
import ru.gb.alex.cloud.server.models.Account;

public class MySQLAuthService implements AuthService {
    private final SessionFactoryUtils factory;

    public MySQLAuthService(SessionFactoryUtils factory) {
        this.factory = factory;
    }

    @Override
    public void start() {
        factory.init();
    }

    @Override
    public void stop() {
        factory.shutdown();
    }

    @Override
    public void changePassword(String username, String newPassword) {
        try (Session session = factory.getSession()) {
            session.beginTransaction();
            session.createQuery("update Account a set a.password = :password where a.username = :username")
                    .setParameter("password", newPassword)
                    .setParameter("username", username)
                    .executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Override
    public boolean createNewAccount(String username, String password) {
        Account account = new Account(username, password);
        try(Session session = factory.getSession()) {
            session.beginTransaction();
            session.persist(account);
            session.getTransaction().commit();
        } catch (PersistenceException e) {
            e.printStackTrace();
            return false;
        }
        return account.getId()!=null;
    }

    @Override
    public boolean authentication(String username, String password) {
        String selectedPassword;
        try(Session session = factory.getSession()) {
            session.beginTransaction();
            selectedPassword = session.createQuery(
                    "select a.password from Account a where a.username = :username", String.class)
                    .setParameter("username", username)
                    .getSingleResult();
            session.getTransaction().commit();
        }
        return selectedPassword.equals(password);
    }
}
