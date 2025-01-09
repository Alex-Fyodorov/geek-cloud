package ru.gb.alex.cloud.client.front;

import ru.gb.alex.cloud.client.constants.CommandForServer;
import ru.gb.alex.cloud.client.handlers.RequestSender;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowListenerImpl implements WindowListener {

    private final RequestSender requestSender;

    public WindowListenerImpl(RequestSender requestSender) {
        this.requestSender = requestSender;
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        requestSender.sendRequest("", CommandForServer.EXIT);
        requestSender.exit();
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
