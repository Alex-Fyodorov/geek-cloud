package others.ioconnection;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Network {

    private String serverAddress;
    private int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Network() throws IOException {
        this.serverAddress = "localhost";
        this.port = 8189;
        initNetworkState(serverAddress, port);
    }

    private void initNetworkState(String serverAddress, int port) throws IOException {
        this.socket = new Socket(serverAddress, port);
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    public void send(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            if (out == null) {
                initNetworkState(serverAddress, port);
            }
            byte[] bytesOfName = file.getName().getBytes();
            out.write(bytesOfName.length);
            out.write(bytesOfName);
            long sizeOfFile = file.length();
            out.writeLong(sizeOfFile);
            int n = 1024;
            byte[] bytes = new byte[n];
            while (sizeOfFile / n >= 1) {
                fileInputStream.read(bytes);
                out.write(bytes);
                sizeOfFile -= n;
            }
            bytes = new byte[(int) sizeOfFile % n];
            fileInputStream.read(bytes);
            out.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        if (out != null) {
            out.close();
        }
        if (in != null) {
            in.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}
