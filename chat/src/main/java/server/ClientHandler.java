package server;

import constants.Constants;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик для конкретного клиента.
 */
public class ClientHandler {

    private MyServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedWriter outText;
    private FileWriter outFile;
    private String name = "";
    private String login = "";
    private File file;
    private File dir;

    public String getName() {
        return name;
    }

    public BufferedWriter getOutText() {
        return outText;
    }

    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            server.service.execute(() -> {
                try {
                    server.service.execute(() -> {
                        try {
                            Thread.sleep(Constants.AUTH_TIME);
                            if (name.equals("")) {
                                closeConnection();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    });
                    authentification();
                    readMessage();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    closeConnection();
                }
            });
        } catch (IOException ex) {
            throw new RuntimeException("Проблемы при создании обработчика.");
        }
    }

    private void openFile() {
        try {
            dir = new File("history");
            dir.mkdir();
            file = new File("history//" + login + ".txt");
            file.createNewFile();
            outFile = new FileWriter("history//" + login + ".txt", true);
            outText = new BufferedWriter(outFile);
            if (file.length() == 0) {
                outText.write(name + "\n");
            } else {
                outText.append("\nНовое подключение.\n");
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    private void authentification() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith(Constants.AUTH_COMMAND)) {
                String[] tokens = str.split("\\s+");
                String nick = server.getAuthService().getNickByLoginAndPass(tokens[1], tokens[2]);
                if (nick != null) {
                    if (server.isNickBusy(nick)) {
                        sendMessage("Пользователь уже в чате.");
                    } else {
                        name = nick;
                        login = tokens[1];
                        openFile();
                        sendMessage(Constants.AUTH_OK_COMMAND + " " + nick);
                        server.broadcastMessage(nick + " вошёл в чат.");
                        server.subscribe(this);
                        server.sendLastMessages(this);
                        return;
                    }
                }
            }
            if (str.startsWith(Constants.NEW_ACCOUNT)) {
                String[] tokens = str.split("\\s+");
                if (server.getAuthService().newAccount(tokens[1], tokens[2], tokens[3])) {
                    name = tokens[3];
                    login = tokens[1];
                    openFile();
                    sendMessage(Constants.AUTH_OK_COMMAND + " " + name);
                    server.broadcastMessage(name + " вошёл в чат.");
                    server.subscribe(this);
                    return;
                } else {
                    sendMessage("Данные логин/ник уже заняты.");
                }
            } else {
                sendMessage("Неверные логин/пароль.");
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() throws IOException {
        while (true) {
            String messageFromClient = in.readUTF();
            System.out.println("Сообщение от " + name + ": " + messageFromClient);
            if (messageFromClient.equals(Constants.END_COMMAND)) {
                server.sendPersonalMessage(name, name, Constants.END_COMMAND);
                break;
            }
            if (messageFromClient.startsWith(Constants.PERSONAL_COMMAND)) {
                String[] words = messageFromClient.split("\\s+");
                String newMessage = "";
                for (int i = 2; i < words.length; i++){
                    newMessage = newMessage + " " + words[i];
                }
                server.sendPersonalMessage(words[1], name, name +
                        " (личн.): " + newMessage);
                outText.append("Для " + words[1] + " (личн.): " + newMessage + "\n");
            } else if (messageFromClient.startsWith(Constants.CHANGE_NICK)) {
                String[] words = messageFromClient.split("\\s+");
                server.getAuthService().changeNick(name, words[1]);
                name = words[1];
                sendMessage(Constants.AUTH_OK_COMMAND + " " + name);
                outText.append(Constants.AUTH_OK_COMMAND + " " + name + "\n");
                sendMessage(server.printClientsList());
            } else {
                server.broadcastMessage(name + ": " + messageFromClient);
            }
        }
    }

    private void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMessage(name + " вышел из чата.");
        try {
            outText.close();
        } catch (IOException ex) {

        }
        try {
            outFile.close();
        } catch (IOException ex) {

        }
        try {
            in.close();
        } catch (IOException ex) {

        }
        try {
            out.close();
        } catch (IOException ex) {

        }
        try {
            socket.close();
        } catch (IOException ex) {

        }
    }
}
