package ru.gb.alex.cloud.client;

import ru.gb.alex.cloud.client.constants.CommandForServer;
import ru.gb.alex.cloud.client.handlers.RequestSender;
import ru.gb.alex.cloud.client.network.Network;

import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) throws Exception {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();

//        RequestSender.sendAuth("user4", "400",
//                Network.getInstance().getCurrentChannel(), CommandForServer.AUTH);
//        Thread.sleep(100);
//        // TODO поставить блокирующую операцию
//        RequestSender.sendRequest("task.md",
//                Network.getInstance().getCurrentChannel(), CommandForServer.SEND_FILE_TO_CLIENT);
//
//        RequestSender.sendFile(Paths.get("./client_storage/space2.png"),
//                Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
//                //Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                // TODO поставить логгер или что-то получше
//                System.out.println("Файл успешно передан.");
//                //Network.getInstance().stop();
//            }
//        });
//
//        RequestSender.sendRequest("space2.png space3.png",
//                Network.getInstance().getCurrentChannel(), CommandForServer.RENAME);
//
//        RequestSender.sendRequest("space3.png",
//                Network.getInstance().getCurrentChannel(), CommandForServer.DELETE);

//        Thread.sleep(2000);
//        ProtoFileSender.sendFile(Paths.get("./chat/space2.png"),
//                Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
//                Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан.");
//                Network.getInstance().stop();
//            }
//        });
    }
}
