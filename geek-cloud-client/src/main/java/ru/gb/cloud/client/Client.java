package ru.gb.cloud.client;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class Client {
    public static void main(String[] args) throws Exception {
        CountDownLatch networkStarter = new CountDownLatch(1);
        new Thread(() -> Network.getInstance().start(networkStarter)).start();
        networkStarter.await();

        AuthSender.sendFile("user4", "400", Network.getInstance().getCurrentChannel());


//        ProtoFileSender.sendFile(Paths.get("./chat/space.png"),
//                Network.getInstance().getCurrentChannel(), future -> {
//            if (!future.isSuccess()) {
//                future.cause().printStackTrace();
//                //Network.getInstance().stop();
//            }
//            if (future.isSuccess()) {
//                System.out.println("Файл успешно передан.");
//                //Network.getInstance().stop();
//            }
//        });
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
