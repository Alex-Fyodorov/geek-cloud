package ru.gb.alex.cloud.client.inter;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.gb.alex.cloud.client.constants.ButtonsCommand;
import ru.gb.alex.cloud.client.constants.CommandForServer;
import ru.gb.alex.cloud.client.handlers.RequestSender;
import ru.gb.alex.cloud.client.network.Network;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class WindowRepresent extends JFrame implements Represent {
    //private enum ButtonsCommand {COPY, MOVE, DELETE}
    private static final int WINDOW_HEIGHT = 400;
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_POS_X = 200;
    private static final int WINDOW_POS_Y = 300;
    private static final String CLIENT_STORAGE = "./client_storage/";
    private static final String TABLE_SERVER = "tableServer";
    private static final String TABLE_CLIENT = "tableClient";
    JButton btnLogin = new JButton("Login");
    JButton btnCopy = new JButton("Copy");
    JButton btnMove = new JButton("Move");
    JButton btnRename = new JButton("Rename");
    JButton btnDelete = new JButton("Delete");
    Logger logger = LogManager.getLogger(WindowRepresent.class);
    private final String[] columnsHeaders = new String[]{"Filename", "Size"};
    private final DataModel modelClient = new DataModel(new String[0][0], columnsHeaders);
    private final DataModel modelServer = new DataModel(new String[0][0], columnsHeaders);
    private CountDownLatch confirmLogin = new CountDownLatch(1);
    private Integer row;
    private String selectedTableName;
    private final List<String> selectedFiles;

    public WindowRepresent() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocation(WINDOW_POS_X, WINDOW_POS_Y);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle("GeekCloud");
        setResizable(false);
        selectedFiles = new ArrayList<>();

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        JPanel panelTop = new JPanel(gridBagLayout);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(0, 10, 0, 0);
        constraints.weightx = 0.7;
        panelTop.add(new JLabel("Client"), constraints);
        constraints.weightx = 0.5;
        panelTop.add(new JLabel("Server"), constraints);
        constraints.anchor = GridBagConstraints.FIRST_LINE_END;
        constraints.weightx = 0;
        constraints.insets = new Insets(5, 0, 5, 5);
        btnLogin.addActionListener(e -> new LoginWindow(this));
        panelTop.add(btnLogin, constraints);
        add(panelTop, BorderLayout.NORTH);

        JPanel panelBottom = new JPanel(gridBagLayout);
        constraints.weightx = 0.5;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(5, 5, 5, 0);
        panelBottom.add(btnCopy, constraints);
        panelBottom.add(btnMove, constraints);
        panelBottom.add(btnRename, constraints);
        constraints.insets = new Insets(5, 5, 5, 5);
        panelBottom.add(btnDelete, constraints);
        add(panelBottom, BorderLayout.SOUTH);

        JTable tableClient = new JTable(modelClient);
        JTable tableServer = new JTable(modelServer);
        tableClient.setName(TABLE_CLIENT);
        tableServer.setName(TABLE_SERVER);
        setTableProperties(tableClient);
        setTableProperties(tableServer);
        showClientFileList();
        addTableListener(tableClient);
        addTableListener(tableServer);

        JPanel tablePanel = new JPanel(gridBagLayout);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weighty = 0.5;
        constraints.insets = new Insets(0, 5, 0, 0);
        tablePanel.add(new JScrollPane(tableClient), constraints);
        constraints.insets = new Insets(0, 5, 0, 5);
        tablePanel.add(new JScrollPane(tableServer), constraints);
        add(tablePanel, BorderLayout.CENTER);

        setVisible(true);
    }

    @Override
    public void setDefaultCloseOperation(int operation) {
        super.setDefaultCloseOperation(operation);
    }

    public void changeLoginButton() {
        btnLogin.setText("Exit");
        btnLogin.addActionListener(e -> System.exit(0));
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    @Override
    public void showServerFileList(String message) {
        String[][] fileList = Arrays.stream(message.split("\\s"))
                .map(f -> f.split("//"))
                .toArray(String[][]::new);
        modelServer.setData(fileList);
        modelServer.fireTableDataChanged();
    }

    @Override
    public void showClientFileList() {
        File[] filesInClientDir = new File(CLIENT_STORAGE).listFiles();
        if (filesInClientDir != null && filesInClientDir.length > 0) {
            String[][] fileList = Arrays.stream(filesInClientDir)
                    .collect(Collectors.toMap(File::getName, File::length))
                    .entrySet().stream()
                    .map(e -> new String[]{e.getKey(), String.valueOf(e.getValue())})
                    .toArray(String[][]::new);
            modelClient.setData(fileList);
            modelClient.fireTableDataChanged();
        }
    }

    @Override
    public void login(String username, String password, CommandForServer command) {
        RequestSender.sendAuth(username, password, getChannel(), command);
        try {
            confirmLogin.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void confirmLogin(boolean confirm) {
        confirmLogin.countDown();
        if (confirm) {
            changeLoginButton();
            btnCopy.addActionListener(e -> buttonsListener(ButtonsCommand.COPY));
            btnMove.addActionListener(e -> buttonsListener(ButtonsCommand.MOVE));
            btnRename.addActionListener(e -> renameListener());
            btnDelete.addActionListener(e -> buttonsListener(ButtonsCommand.DELETE));
        } else confirmLogin = new CountDownLatch(1);
    }

    private void setTableProperties(JTable table) {
//        table.setForeground(Color.red);
//        table.setSelectionForeground(Color.yellow);
//        table.setSelectionBackground(Color.blue);
        table.setShowGrid(false);
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table,
                        value, isSelected, hasFocus, row, column);

                if (column == 1) label.setHorizontalAlignment(JLabel.RIGHT);
                else label.setHorizontalAlignment(JLabel.LEFT);
                return label;
            }
        });
    }

    private void addTableListener(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
                    row = table.rowAtPoint(e.getPoint());
                    selectedTableName = table.getName();
                    table.addRowSelectionInterval(row, row);
                }
                if ((e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) {
                    int currentRow = table.rowAtPoint(e.getPoint());
                    String currentTableName = table.getName();
                    if (currentTableName.equals(selectedTableName) && row != null) {
                        table.addRowSelectionInterval(currentRow, row);
                    }
                }
                row = table.rowAtPoint(e.getPoint());
                selectedTableName = table.getName();
                getSelectedFileList(table);
            }
        });
    }

    private void getSelectedFileList(JTable table) {
        selectedFiles.clear();
        Arrays.stream(table.getSelectedRows())
                .mapToObj(i -> (String) table.getValueAt(i, 0))
                .forEach(selectedFiles::add);
    }

    private void buttonsListener(ButtonsCommand command) {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files are selected");
            return;
        }
        if (actionMessage(command) == JOptionPane.YES_OPTION) {
            if (selectedTableName.equals(TABLE_SERVER)) {
                serverRequest(command);
            } else {
                try {
                    clientRequest(command);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void renameListener() {
        if (selectedFiles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No files are selected");
            return;
        }
        if (selectedFiles.size() > 1) {
            JOptionPane.showMessageDialog(this, "More than one file selected");
            return;
        }
        String fileName = selectedFiles.get(0);
        JTextField newNameField = new JTextField(fileName);
        Object[] paneContent = new Object[]{"Input new file name", newNameField};
        int result = JOptionPane.showConfirmDialog(this, paneContent,
                "New file name", JOptionPane.OK_CANCEL_OPTION);
        String newName = String.valueOf(newNameField.getText());
        System.out.println(newName);
        if (result == JOptionPane.OK_OPTION) {
            if (selectedTableName.equals(TABLE_SERVER)) {
                RequestSender.sendRequest(String.format("%s %s", fileName, newName),
                        getChannel(), CommandForServer.RENAME);
            } else if (selectedTableName.equals(TABLE_CLIENT)) {
                List<String> exceptionList = checkRenamableFiles(fileName, newName);
                if (exceptionList.isEmpty()) {
                    try {
                        Files.move(Paths.get(CLIENT_STORAGE + fileName),
                                Paths.get(CLIENT_STORAGE + newName), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    showClientFileList();
                } else {
                    JOptionPane.showMessageDialog(this, exceptionList.toArray(),
                            "Warning!", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private List<String> checkRenamableFiles(String oldName, String newName) {
        List<String> exceptionList = new ArrayList<>();
        if (!Files.exists(Paths.get(CLIENT_STORAGE + oldName))) {
            exceptionList.add(String.format("File \"%s\" does not exist.", oldName));
        }
        if (Files.exists(Paths.get(CLIENT_STORAGE + newName))) {
            exceptionList.add(String.format("File \"%s\" already exists.", newName));
        }
        return exceptionList;
    }

    private void clientRequest(ButtonsCommand command) throws IOException, InterruptedException {
        if (command != ButtonsCommand.DELETE) {
            for (String f : selectedFiles) {
                CountDownLatch countDownLatch = new CountDownLatch(1);
                Path filePath = Paths.get(CLIENT_STORAGE + f);
                RequestSender.sendFile(filePath, getChannel(), future -> {
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
                Files.delete(Paths.get(CLIENT_STORAGE + f));
            }
        }
        Thread.sleep(50);
        showClientFileList();
    }

    private void serverRequest(ButtonsCommand command) {
        if (command != ButtonsCommand.DELETE) {
            selectedFiles.forEach(f -> {
                RequestSender.sendRequest(f, getChannel(), CommandForServer.SEND_FILE);
            });
        }
        if (command != ButtonsCommand.COPY) {
            selectedFiles.forEach(f -> {
                RequestSender.sendRequest(f, getChannel(), CommandForServer.DELETE);
            });
        }
        showClientFileList();
    }

    private int actionMessage(ButtonsCommand command) {
        StringBuilder message = new StringBuilder("\nDo you really want\nto ")
                .append(command.getMessage())
                .append(" the selected files\nfrom the ");
        if (selectedTableName.equals(TABLE_SERVER)) message.append("Server to the Client?");
        else message.append("Client to the Server?");
        if (command == ButtonsCommand.DELETE) {
            message.delete(64, message.length())
                    .append(" storage?");
        }

        List<String> selectedFilesCopy = new ArrayList<>(selectedFiles);
        selectedFilesCopy.add(message.toString());

        return JOptionPane.showConfirmDialog(this,
                selectedFilesCopy.toArray(),
                "Confirm the action!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
    }

    private Channel getChannel() {
        return Network.getInstance().getCurrentChannel();
    }
}

// TODO сделать выход, не вызывающий ошибок
// TODO удаление последней строки в таблице приводит к ошибке

/*
Проблемы:
1. На данный момент приложение передаёт только файлы до 4,3 Гб,
   после чего жалуется, что закончилась память.
 */
