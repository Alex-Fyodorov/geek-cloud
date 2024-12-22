package ru.gb.serialization;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {

        try (Socket socket = new Socket("localhost", 8189);
             ObjectEncoderOutputStream encoderOut =
                     new ObjectEncoderOutputStream(socket.getOutputStream());
             ObjectDecoderInputStream decoderIn =
                     new ObjectDecoderInputStream(socket.getInputStream(), 100 * 1024 * 1024)) {
            MyMessage textMessage = new MyMessage("Hello Server!");
            encoderOut.writeObject(textMessage);
            encoderOut.flush();

            MyMessage messageFromServer = (MyMessage) decoderIn.readObject();
            System.out.println("Answer from Server: " + messageFromServer.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
