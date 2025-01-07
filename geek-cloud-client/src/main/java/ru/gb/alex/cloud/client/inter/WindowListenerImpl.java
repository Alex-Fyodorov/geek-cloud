package ru.gb.alex.cloud.client.inter;

import ru.gb.alex.cloud.client.constants.CommandForServer;
import ru.gb.alex.cloud.client.handlers.RequestSender;
import ru.gb.alex.cloud.client.network.Network;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class WindowListenerImpl implements WindowListener {

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        RequestSender.sendRequest("", Network.getInstance().getCurrentChannel(),
                CommandForServer.EXIT);
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
