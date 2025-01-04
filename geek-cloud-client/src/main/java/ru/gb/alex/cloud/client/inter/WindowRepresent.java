package ru.gb.alex.cloud.client.inter;

import io.netty.channel.Channel;
import ru.gb.alex.cloud.client.handlers.RequestSender;
import ru.gb.alex.cloud.client.network.Network;
import ru.gb.alex.cloud.client.constants.CommandForServer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.concurrent.CountDownLatch;

public class WindowRepresent extends JFrame implements Represent {

    private static final int WINDOW_HEIGHT = 400;
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_POS_X = 200;
    private static final int WINDOW_POS_Y = 300;
    JButton btnLogin = new JButton("Login");
    JButton btnCopy = new JButton("Copy");
    JButton btnMove = new JButton("Move");
    JButton btnRename = new JButton("Rename");
    JButton btnDelete = new JButton("Delete");
    private String[] columnsHeaders = new String[]{"Filename", "Size"};
    private String[] columnsHeaders1 = new String[]{"Filename1", "Size1"};
    private String[][] columnsHeaders2 = new String[][]{{"File111111111111111", "Size1"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}};
    private CountDownLatch confirmLogin = new CountDownLatch(1);

    public WindowRepresent() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocation(WINDOW_POS_X, WINDOW_POS_Y);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setTitle("GeekCloud");
        setResizable(false);

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

        btnCopy.addActionListener(e -> new LoginWindow(this)); // TODO listener
        btnMove.addActionListener(e -> new LoginWindow(this)); // TODO listener
        btnRename.addActionListener(e -> new LoginWindow(this)); // TODO listener
        btnDelete.addActionListener(e -> new LoginWindow(this)); // TODO listener
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

        JTable tableClient = new JTable(new DataModel(columnsHeaders2, columnsHeaders));
        setTableProperties(tableClient);

        //tableClient.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JTable tableServer = new JTable(new DataModel(columnsHeaders2, columnsHeaders1));
        setTableProperties(tableServer);
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

    }

    @Override
    public void showClientFileList() {

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
    public void confirmLogin() {
        confirmLogin.countDown();
        changeLoginButton();
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
                JLabel label = (JLabel)super.getTableCellRendererComponent(table,
                        (String) value, isSelected, hasFocus, row, column);

                if (column == 1) label.setHorizontalAlignment(JLabel.RIGHT);
                else label.setHorizontalAlignment(JLabel.LEFT);
                return label;
            }
        });
    }

    private Channel getChannel() {
        return Network.getInstance().getCurrentChannel();
    }
}
