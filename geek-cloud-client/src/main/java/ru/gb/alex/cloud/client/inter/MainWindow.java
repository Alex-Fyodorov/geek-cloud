package ru.gb.alex.cloud.client.inter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class MainWindow extends JFrame {

    private static final int WINDOW_HEIGHT = 400;
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_POS_X = 200;
    private static final int WINDOW_POS_Y = 300;
    JButton btnInput = new JButton("Input");
    JButton btnCopy = new JButton("Copy");
    JButton btnMove = new JButton("Move");
    JButton btnRename = new JButton("Rename");
    JButton btnDelete = new JButton("Delete");
    private String[] columnsHeaders = new String[]{"Filename", "Size"};
    private String[][] columnsHeaders2 = new String[][]{{"File111111111111111", "Size1"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}, {"File2", "Size2"}};

    public MainWindow() {
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
        btnInput.addActionListener(e -> new LoginWindow(this)); // TODO listener
        panelTop.add(btnInput, constraints);
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
//        tableClient.setForeground(Color.red);
//        tableClient.setSelectionForeground(Color.yellow);
//        tableClient.setSelectionBackground(Color.blue);
//        tableClient.setShowGrid(false);
        tableClient.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
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
        //tableClient.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JTable tableServer = new JTable(columnsHeaders2, columnsHeaders);
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
        btnInput.setText("Exit");
        btnInput.addActionListener(e -> System.exit(0));
    }
}
