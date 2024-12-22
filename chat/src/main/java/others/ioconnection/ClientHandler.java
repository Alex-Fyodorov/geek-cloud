package others.ioconnection;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class ClientHandler {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessages() throws IOException {
        int sizeOfName = in.read();
        byte[] bytesOfName = new byte[sizeOfName];
        in.read(bytesOfName);
        String nameOfFile = new String(bytesOfName);
        long sizeOfFile = in.readLong();
        System.out.println(sizeOfFile);
        File file = new File("chat/2/3/4/" + nameOfFile);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        int n = 1024;
        byte[] bytes = new byte[n];
        while (sizeOfFile / n >= 1) {
            in.read(bytes);
            fileOutputStream.write(bytes);
            sizeOfFile -= n;
        }
        bytes = new byte[(int) sizeOfFile % n];
        in.read(bytes);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
    }

    public void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
