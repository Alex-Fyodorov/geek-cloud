package ru.gb.callback_example;

import javax.swing.*;
import java.awt.*;

public class MySwingApp extends JFrame {
    public MySwingApp() throws HeadlessException {
        setTitle("A");
        setBounds(500, 400, 200, 200);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        JButton btn = new JButton("Send");
//        btn.addActionListener((e) -> {
//            MyNet.sendMessage(() -> {
//                JOptionPane.showConfirmDialog(null, "Всё отправлено!");
//            });
//        });
//        add(btn);
        JButton btn2 = new JButton("Send2");
        btn2.addActionListener((e) -> {
            MyNet.fileReceived((path) -> {
                JOptionPane.showConfirmDialog(null, "File received: " + path.getFileName().toString());
            });
        });
        add(btn2);
        setVisible(true);
    }

    public static void main(String[] args) {
        new MySwingApp();
    }
}
