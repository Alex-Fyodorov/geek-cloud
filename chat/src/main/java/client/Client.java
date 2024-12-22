package client;

import constants.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends JFrame {

    private JTextField textField;
    private JTextArea textArea;
    private JTextArea nickArea;
    private JTextField loginField;
    private JTextField passField;

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private boolean auth;

    public Client(){
        try {
            openConnection();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        prepareUI();
    }

    private void openConnection() throws IOException {
        socket = new Socket(Constants.SERVER_ADDRESS, Constants.SERVER_PORT);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        new Thread(() -> {
            try{
                while (true){

                    new Thread(() -> {
                        try {
                            Thread.sleep(Constants.AUTH_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (!auth){
                            closeConnection();
                        }
                    }).start();

                    String messageFromServer = dataInputStream.readUTF();
                    if (messageFromServer.equals(Constants.END_COMMAND)) {
                        closeConnection();
                        break;
                    }
                    if (messageFromServer.startsWith(Constants.LIST_COMMAND)) {
                        String[] nicks = messageFromServer.split("\\s+");
                        nickArea.setText("Список:\n");
                        for (int i = 1; i < nicks.length; i++){
                            nickArea.append(nicks[i]);
                            nickArea.append("\n");
                        }
                    } else if (messageFromServer.startsWith(Constants.AUTH_OK_COMMAND)){
                        auth = true;
                        StringBuilder nick = new StringBuilder(messageFromServer);
                        nick.delete(0, 7);
                        setTitle(nick.toString());
                    } else {
                        textArea.append(messageFromServer);
                        textArea.append("\n");
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }).start();
    }

    private void closeConnection() {
        textArea.append("Соединение разорвано");
        textField.setEnabled(false);
        loginField.setEnabled(false);
        passField.setEnabled(false);
        try {
            dataOutputStream.close();
        }catch (Exception ex){

        }
        try {
            dataInputStream.close();
        }catch (Exception ex){

        }
        try {
            socket.close();
        }catch (Exception ex){

        }
        setTitle("");
    }

    private void sendMessage() {
        if (textField.getText().trim().isEmpty()) {
            return;
        }
        try{
            dataOutputStream.writeUTF(textField.getText());
            textField.setText("");
            textField.grabFocus();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void prepareUI() {
        setBounds(200,200,500,500);
        setTitle("Неавторизованный пользователь");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        JButton button = new JButton("Send");
        panel.add(button, BorderLayout.EAST);
        textField = new JTextField();
        panel.add(textField, BorderLayout.CENTER);

        add(panel, BorderLayout.SOUTH);

        JPanel loginPanel = new JPanel(new GridLayout(1, 3));
        loginField = new JTextField();
        passField = new JTextField();
        JButton authButton = new JButton("Авторизация");
        loginPanel.add(loginField);
        loginPanel.add(passField);
        loginPanel.add(authButton);
        add(loginPanel, BorderLayout.NORTH);

        nickArea = new JTextArea();
        nickArea.setEditable(false);
        nickArea.setLineWrap(true);
        add(new JScrollPane(nickArea), BorderLayout.EAST);

        authButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    dataOutputStream.writeUTF(Constants.AUTH_COMMAND +
                            " " + loginField.getText() + " " + passField.getText());
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}