package others;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class TempApp {
    // Получение размеров всех файлов
    public static void main(String[] args) throws IOException {
        List<Long> sizes = Files.list(Paths.get("."))
                .filter(p -> !Files.isDirectory(p))
                .map(Path::toFile)
                .map(File::length)
                .collect(Collectors.toList());
        System.out.println(sizes);

        File[] filesInCurrentDir = new File(".").listFiles();
        for (File file : filesInCurrentDir) {
            System.out.println(file.getName() + ": " + file.length());
        }
    }
}
