package others;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class NioLecture {
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("chat", "2", "3", "1.txt");
        byte[] data = "Java".getBytes(StandardCharsets.UTF_8);
        System.out.println(Files.isWritable(path));
        //Записать в файл
        Files.write(path, data, StandardOpenOption.APPEND);
        byte[] data1 = Files.readAllBytes(path);
        String str = new String(Files.readAllBytes(path));
        System.out.println(str);
        //Пройти по дереву каталогов
        Files.walkFileTree(Paths.get("chat", "2"),
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file.getFileName());
                        return FileVisitResult.CONTINUE;
                    }
                });
        Path path2 = Paths.get("chat", "2", "3", "4", "5.txt");
        //Вычитать файл построчно, получаем стрим строк.
        Files.lines(path2).forEach(System.out::println);
        //копирование файла, можно в поток
        Files.copy(path2, Paths.get("chat", "2", "2.txt"),
                StandardCopyOption.REPLACE_EXISTING);

        RandomAccessFile src = new RandomAccessFile("chat/space.png", "rw");
        RandomAccessFile dst = new RandomAccessFile("chat/space2.png", "rw");
        FileChannel srcCh = src.getChannel();
        FileChannel dstCh = dst.getChannel();
        //оба варианта делают одно и то же
        //srcCh.transferTo(0, src.length(), dstCh);
        dstCh.transferFrom(srcCh, 0, src.length());

        RandomAccessFile file = new RandomAccessFile("chat/111.txt", "rw");
        FileChannel channel = file.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        int bytesRead = channel.read(buffer);
        while (bytesRead > -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                System.out.print((char)buffer.get());
            }
            buffer.clear();
            bytesRead = channel.read(buffer);
        }
        file.close();
    }
}
