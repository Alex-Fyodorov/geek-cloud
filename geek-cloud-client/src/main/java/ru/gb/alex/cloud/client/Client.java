package ru.gb.alex.cloud.client;

import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public class Client {
    public static void main(String[] args) throws Exception {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();

        FileSender.sendAuth("user4", "400", Network.getInstance().getCurrentChannel());
        Thread.sleep(10000);
        // TODO поставить блокирующую операцию
        FileSender.fileRequest("task.md", Network.getInstance().getCurrentChannel());

        FileSender.sendFile(Paths.get("./client_storage/space2.png"),
                Network.getInstance().getCurrentChannel(), future -> {
            if (!future.isSuccess()) {
                future.cause().printStackTrace();
                //Network.getInstance().stop();
            }
            if (future.isSuccess()) {
                System.out.println("Файл успешно передан.");
                //Network.getInstance().stop();
            }
        });

        FileSender.renameFile("space2.png space3.png", Network.getInstance().getCurrentChannel());

        FileSender.deleteFile("space3.png", Network.getInstance().getCurrentChannel());

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
