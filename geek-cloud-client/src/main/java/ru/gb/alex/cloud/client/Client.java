package ru.gb.alex.cloud.client;

import ru.gb.alex.cloud.client.front.Represent;
import ru.gb.alex.cloud.client.front.WindowRepresent;
import ru.gb.alex.cloud.client.handlers.InClientHandler;
import ru.gb.alex.cloud.client.handlers.RequestSender;
import ru.gb.alex.cloud.client.network.Network;

import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) throws Exception {
        Network network = new Network();
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> network.start(networkStarter)).start();
        networkStarter.await();
        RequestSender requestSender = new RequestSender(network);
        Represent represent = new WindowRepresent(requestSender);
        network.getCurrentChannel().pipeline().addLast(new InClientHandler(represent));
    }
}
