package others.ioconnection;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public Server() {
        try (ServerSocket server = new ServerSocket(8189)){
            Network network = new Network();
            Socket socket = server.accept();
            ClientHandler clientHandler = new ClientHandler(socket);
            network.send(new File("chat/space.png"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
