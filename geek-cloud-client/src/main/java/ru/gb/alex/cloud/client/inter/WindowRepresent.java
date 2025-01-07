package ru.gb.alex.cloud.client.inter;

import io.netty.channel.Channel;
import ru.gb.alex.cloud.client.constants.ButtonsCommand;
import ru.gb.alex.cloud.client.constants.CommandForServer;
import ru.gb.alex.cloud.client.constants.StringConstants;
import ru.gb.alex.cloud.client.handlers.RequestSender;
import ru.gb.alex.cloud.client.network.Network;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    JButton btnLogin = new JButton("Login");
    JButton btnCopy = new JButton("Copy");
    JButton btnMove = new JButton("Move");
    JButton btnRename = new JButton("Rename");
    JButton btnDelete = new JButton("Delete");
    private final String[] columnsHeaders = new String[]{"Filename", "Size"};
    private final DataModel modelClient = new DataModel(new String[0][0], columnsHeaders);
    private final DataModel modelServer = new DataModel(new String[0][0], columnsHeaders);
    private final ButtonsListener buttonsListener;
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
        buttonsListener = new ButtonsListener(this);
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
        tableClient.setName(StringConstants.TABLE_CLIENT);
        tableServer.setName(StringConstants.TABLE_SERVER);
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
        File[] filesInClientDir = new File(StringConstants.CLIENT_STORAGE).listFiles();
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
            btnCopy.addActionListener(e -> buttonsListener.buttonsListener(ButtonsCommand.COPY, selectedFiles, selectedTableName));
            btnMove.addActionListener(e -> buttonsListener.buttonsListener(ButtonsCommand.MOVE, selectedFiles, selectedTableName));
            btnRename.addActionListener(e -> buttonsListener.renameListener(selectedFiles, selectedTableName));
            btnDelete.addActionListener(e -> buttonsListener.buttonsListener(ButtonsCommand.DELETE, selectedFiles, selectedTableName));
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
