package ru.gb.alex.cloud.client.front;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.client.constants.ButtonsCommand;
import ru.gb.alex.cloud.client.constants.CommandForServer;
import ru.gb.alex.cloud.client.constants.StringConstants;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ButtonsListener {
    private final WindowRepresent mainWindow;
    private final Logger logger;

    public ButtonsListener(WindowRepresent mainWindow) {
        this.mainWindow = mainWindow;
        logger = LogManager.getLogger(ButtonsListener.class);
    }

    public void buttonsListener(ButtonsCommand command,
                                 List<String> selectedFiles, String selectedTableName) {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "No files are selected");
            return;
        }
        if (actionMessage(command, selectedFiles, selectedTableName) == JOptionPane.YES_OPTION) {
            if (selectedTableName.equals(StringConstants.TABLE_SERVER)) {
                serverRequest(command, selectedFiles);
            } else {
                try {
                    clientRequest(command, selectedFiles);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void clientRequest(ButtonsCommand command, List<String> selectedFiles) throws IOException, InterruptedException {
        if (command != ButtonsCommand.DELETE) {
            for (String f : selectedFiles) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Path filePath = Paths.get(StringConstants.CLIENT_STORAGE + f);
                mainWindow.getRequestSender().sendFile(filePath, future -> {
                    if (future.isSuccess()) {
                        logger.info("The file has been sent successfully: " + filePath);
                    }
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                        logger.info("Sending the file failed: " + filePath);
                    }
                    countDownLatch.countDown();
                });
                countDownLatch.await();
            }
        }
        if (command != ButtonsCommand.COPY) {
            for (String f : selectedFiles) {
                Files.delete(Paths.get(StringConstants.CLIENT_STORAGE + f));
            }
        }
        Thread.sleep(50);
        mainWindow.showClientFileList();
    }

    private void serverRequest(ButtonsCommand command, List<String> selectedFiles) {
        if (command != ButtonsCommand.DELETE) {
            selectedFiles.forEach(f -> {
                mainWindow.getRequestSender().sendRequest(f, CommandForServer.SEND_FILE);
            });
        }
        if (command != ButtonsCommand.COPY) {
            selectedFiles.forEach(f -> {
                mainWindow.getRequestSender().sendRequest(f, CommandForServer.DELETE);
            });
        }
        mainWindow.showClientFileList();
    }

    private int actionMessage(ButtonsCommand command,
                              List<String> selectedFiles, String selectedTableName) {
        StringBuilder message = new StringBuilder("\nDo you really want\nto ")
                .append(command.getMessage())
                .append(" the selected files\nfrom the ");
        if (selectedTableName.equals(StringConstants.TABLE_SERVER)) message.append("Server to the Client?");
        else message.append("Client to the Server?");
        if (command == ButtonsCommand.DELETE) {
            message.delete(64, message.length())
                    .append(" storage?");
        }

        List<String> selectedFilesCopy = new ArrayList<>(selectedFiles);
        selectedFilesCopy.add(message.toString());

        return JOptionPane.showConfirmDialog(mainWindow,
                selectedFilesCopy.toArray(),
                "Confirm the action!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    public void renameListener(List<String> selectedFiles, String selectedTableName) {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(mainWindow, "No files are selected");
            return;
        }
        if (selectedFiles.size() > 1) {
            JOptionPane.showMessageDialog(mainWindow, "More than one file selected");
            return;
        }
        String fileName = selectedFiles.get(0);
        JTextField newNameField = new JTextField(fileName);
        Object[] paneContent = new Object[]{"Input new file name", newNameField};
        int result = JOptionPane.showConfirmDialog(mainWindow, paneContent,
                "New file name", JOptionPane.OK_CANCEL_OPTION);
        String newName = String.valueOf(newNameField.getText());
        System.out.println(newName);
        if (result == JOptionPane.OK_OPTION) {
            if (selectedTableName.equals(StringConstants.TABLE_SERVER)) {
                mainWindow.getRequestSender().sendRequest(String.format("%s %s", fileName, newName),
                        CommandForServer.RENAME);
            } else if (selectedTableName.equals(StringConstants.TABLE_CLIENT)) {
                List<String> exceptionList = checkRenamableFiles(fileName, newName);
                if (exceptionList.isEmpty()) {
                    try {
                        Files.move(Paths.get(StringConstants.CLIENT_STORAGE + fileName),
                                Paths.get(StringConstants.CLIENT_STORAGE + newName), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    mainWindow.showClientFileList();
                } else {
                    JOptionPane.showMessageDialog(mainWindow, exceptionList.toArray(),
                            "Warning!", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private List<String> checkRenamableFiles(String oldName, String newName) {
        List<String> exceptionList = new ArrayList<>();
        if (!Files.exists(Paths.get(StringConstants.CLIENT_STORAGE + oldName))) {
            exceptionList.add(String.format("File \"%s\" does not exist.", oldName));
        }
        if (Files.exists(Paths.get(StringConstants.CLIENT_STORAGE + newName))) {
            exceptionList.add(String.format("File \"%s\" already exists.", newName));
        }
        return exceptionList;
    }

    public void exit() {
        mainWindow.getRequestSender().sendRequest("", CommandForServer.EXIT);
        mainWindow.getRequestSender().exit();
        System.exit(0);
    }
}
