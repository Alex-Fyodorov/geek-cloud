package ru.gb.blockserver_commented;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class MyClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8189);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             Scanner in = new Scanner(socket.getInputStream())) {
            out.write(new byte[]{11, 21, 31});
            String x = in.nextLine();
            System.out.println("A: " + x);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
