package ch.papers.androidcommunicationbenchmark.communication.wifi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.papers.androidcommunicationbenchmark.communication.Constants;
import ch.papers.androidcommunicationbenchmark.communication.EchoServerHandler;
import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class WifiServer implements Server {
    private boolean isRunning = false;



    @Override
    public void start() {
        this.isRunning = true;
        try {
            ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT);
            Socket socket;
            while ((socket = serverSocket.accept()) != null && this.isRunning) {
                Logger.getInstance().log("wifiserver", "accepted connection");
                new Thread(new EchoServerHandler(socket.getInputStream(), socket.getOutputStream())).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        this.isRunning = false;
    }
}
