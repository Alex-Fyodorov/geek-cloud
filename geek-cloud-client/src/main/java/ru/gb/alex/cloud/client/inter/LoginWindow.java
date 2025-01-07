package ru.gb.alex.cloud.client.inter;

import ru.gb.alex.cloud.client.constants.CommandForServer;

import javax.swing.*;
import java.awt.*;

public class LoginWindow extends JFrame {

    private static final int WINDOW_HEIGHT = 170;
    private static final int WINDOW_WIDTH = 300;
    JButton btnOk = new JButton("Ok");
    JButton btnClose = new JButton("Close");
    JCheckBox cbNewAcc = new JCheckBox("New account");
    JTextField loginField = new JTextField();
    JPasswordField passField = new JPasswordField();

    public LoginWindow(WindowRepresent windowRepresent) {
        getRootPane().setDefaultButton(btnOk);
        setLocationRelativeTo(windowRepresent);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        btnOk.addActionListener(e -> login(windowRepresent));
        btnClose.addActionListener(e -> dispose());

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        JPanel settingsPanel = new JPanel(gridBagLayout);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 0, 0);
        constraints.weightx = 0.0;
        settingsPanel.add(new JLabel("Login"), constraints);
        constraints.insets = new Insets(10, 10, 0, 10);
        constraints.weightx = 0.9;
        settingsPanel.add(loginField, constraints);

        constraints.gridy = 1;
        constraints.insets = new Insets(10, 10, 0, 0);
        constraints.weightx = 0.0;
        settingsPanel.add(new JLabel("Password"), constraints);
        constraints.insets = new Insets(10, 10, 0, 10);
        constraints.weightx = 0.9;
        settingsPanel.add(passField, constraints);

        constraints.gridy = 2;
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.insets = new Insets(5, 0, 0, 10);
        constraints.weightx = 0.0;
        settingsPanel.add(cbNewAcc, constraints);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonPanel.add(btnOk);
        buttonPanel.add(btnClose);

        constraints.gridy = 3;
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.LINE_END;
        constraints.insets = new Insets(5, 10, 10, 10);
        constraints.weightx = 0.0;
        settingsPanel.add(buttonPanel, constraints);

        add(settingsPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void login(WindowRepresent windowRepresent) {
        String login = loginField.getText();
        String password = String.valueOf(passField.getPassword());
        if (login == null || login.isBlank() || password.isBlank()) {
            JOptionPane.showMessageDialog(this, "Login or password is not filled in.");
            return;
        }
        if (cbNewAcc.isSelected()) {
            JPasswordField passwordField = new JPasswordField();
            Object[] paneContent = new Object[]{"Confirm the password", passwordField};
            JOptionPane.showMessageDialog(this, paneContent,
                    "Confirm", JOptionPane.PLAIN_MESSAGE);
            String confirmPassword = String.valueOf(passwordField.getPassword());
            if (password.equals(confirmPassword)) {
                windowRepresent.login(login, password, CommandForServer.REG);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid password",
                        "Invalid password", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            windowRepresent.login(login, password, CommandForServer.AUTH);
        }
        dispose();
    }
}
