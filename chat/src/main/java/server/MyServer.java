package server;

import constants.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Логика сервера.
 */
public class MyServer {

    /**
     * Сервис аутентификации.
     */
    private AuthService authServiсe;

    /**
     * Активные клиенты.
     */
    private List<ClientHandler> clients;

    private BufferedWriter outText;
    private FileWriter outFile;
    private File file;
    private File dir;
    private long count;
    public ExecutorService service;

    Logger logger = LogManager.getLogger(MyServer.class);

    public AuthService getAuthService() {
        return authServiсe;
    }

    public MyServer(){
        try (ServerSocket server = new ServerSocket(Constants.SERVER_PORT)){
            authServiсe = new DataBaseAuthService();
            authServiсe.start();
            clients = new ArrayList<>();
            service = Executors.newCachedThreadPool();
            openFile();
            count = 0; //Счётчик строк, используется как индекс строки.

            while (true){
                logger.info("Сервер ожидает подключения.");
                Socket socket = server.accept();
                logger.info("Клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException ex){
            logger.warn("Ошибка в работе сервера.");
            ex.printStackTrace();
        } finally {
            if (authServiсe != null) {
                authServiсe.stop();
            }
            closeFile();
            service.shutdown();
        }
    }

    /**
     * Метод:
     * 1. Создаёт папку history, в которую будет писаться история файла.
     *    Поскольку сервер первичен, подобная конструкция в ClientHandler
     *    становится ненужна.
     * 2. Создаёт файл history.txt, если его нет. История с прежних
     *    сессий не хранится. С каждым запуском сервера история сообщений
     *    пишется заново. В ClientHandler наоборот, история прошлых
     *    подключений сохраняется.
     * 3. Открывает потоки на запись в файл.
     * 4. Не открывает потоки на чтение. Поскольку прочитав файл один раз
     *    поток откатиться не может, его приходится открывать каждый раз
     *    заново.
     */
    private void openFile() {
        try {
            dir = new File("history");
            dir.mkdir();
            file = new File("history//history.txt");
            file.createNewFile();
            outFile = new FileWriter("history//history.txt");
            outText = new BufferedWriter(outFile);
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * Данный метод теперь не только рассылает сообщение всем участникам
     * беседы, но и пишет историю. Историей занимается именно этот метод,
     * так как личные сообщения должны остаться личными и не должны
     * попадать новым участникам. Из всех сообщений отфильтровываются те,
     * что предназначаются для окна со списком подключенных клиентов,
     * и в начале каждой строки добавляется её порядковый номер.
     * @param message Сообщение.
     */
    public synchronized void broadcastMessage(String message) {

        //clients.forEach(client -> client.sendMessage(message));
        logger.info(message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
            try {
                client.getOutText().append(message + "\n");
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        try {
            if (!message.startsWith(Constants.LIST_COMMAND)) {
                outText.write("" + count + " " + message + "\n");
                count++;
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    public synchronized void sendPersonalMessage(String nick, String author, String message) {
        try {
            logger.info(message);
            for (ClientHandler client : clients){
                if (client.getName().equals(nick)) {
                    client.sendMessage(message);
                    client.getOutText().append(message + "\n");
                    return;
                }
            }
            for (ClientHandler client : clients){
                if (client.getName().equals(author)) {
                    client.sendMessage("Адресат не найден.");
                    client.getOutText().append("Адресат не найден.\n");
                    return;
                }
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    /**
     * Метод отсылает вновь подключившемуся клиенту последние 100
     * сообщений из файла history.txt.
     * @param client
     */
    public void sendLastMessages(ClientHandler client) {
        long index;
        String str = null;
        if (count < 100) {
            index = 0;
        } else index = count - 100;
        try (BufferedReader inText = new BufferedReader(
                new FileReader("history//history.txt"))) {
            outText.flush();
            str = inText.readLine();
            while (!str.startsWith("" + index)) {
                str = inText.readLine();
            }
            while (str != null) {
                String[] arr = str.split("\\s+");
                int length = arr[0].length();

                StringBuilder sb = new StringBuilder(str);
                client.sendMessage(String.valueOf(
                        sb.replace(0,length+1,"")));
                str = inText.readLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler client){
        clients.add(client);
        broadcastMessage(printClientsList());
    }

    public synchronized void unsubscribe(ClientHandler client){
        clients.remove(client);
        broadcastMessage(printClientsList());
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client : clients) {
            if (client.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized String printClientsList(){
        StringBuilder nicksList = new StringBuilder(Constants.LIST_COMMAND);
        for (ClientHandler cl : clients){
            nicksList.append(" " + cl.getName());
        }
        return nicksList.toString();
    }

    private void closeFile() {
        try {
            outText.close();
        } catch (IOException ex) {

        }
        try {
            outFile.close();
        } catch (IOException ex) {

        }
    }
}
