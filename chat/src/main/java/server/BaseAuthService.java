package server;

import java.util.ArrayList;
import java.util.List;

public class BaseAuthService implements AuthService {

    private List<Entry> entries;

    public BaseAuthService(){
        entries = new ArrayList<>();
        entries.add(new Entry("l1", "p1", "nick1"));
        entries.add(new Entry("l2", "p2", "nick2"));
        entries.add(new Entry("l3", "p3", "nick3"));
    }

    @Override
    public void start() {
        System.out.println("Auth service is running.");
    }

    @Override
    public void stop() {
        System.out.println("Auth service is shutting down.");
    }

    @Override
    public String getNickByLoginAndPass(String login, String pass) {
        for (Entry entry: entries){
            if (entry.login.equals(login) && entry.password.equals(pass)){
                return entry.nick;
            }
        }
        return null;
    }

    private class Entry {
        private String login;
        private String password;
        private String nick;

        public Entry(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }
    }

    @Override
    public void changeNick(String oldNick, String newNick) {

    }

    @Override
    public boolean newAccount(String login, String pass, String nick) {
        return false;
    }
}
