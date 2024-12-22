package ru.gb.callback_example;

public class MyConsoleApp {
    public static void main(String[] args) {
        MyNet.sendMessage(() -> {
            System.out.println("Всё отправлено!");
        });
    }
}
