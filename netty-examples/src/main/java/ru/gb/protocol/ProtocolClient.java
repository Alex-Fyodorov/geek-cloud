package ru.gb.protocol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProtocolClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8189);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            out.write(15);
            byte[] filenameBytes = "111.txt".getBytes();
            out.writeInt(filenameBytes.length);
            out.write(filenameBytes);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
