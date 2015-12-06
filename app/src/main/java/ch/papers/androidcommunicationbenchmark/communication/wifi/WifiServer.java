package ch.papers.androidcommunicationbenchmark.communication.wifi;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.communication.EchoServerHandler;
import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class WifiServer implements Server {
    private final Context context;

    private boolean isRunning = false;
    private ServerSocket serverSocket;

    public WifiServer(Context context) {
        this.context = context;
    }

    @Override
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isRunning = true;
                try {
                    makeDiscoverable();
                    serverSocket = new ServerSocket(Constants.SERVER_PORT);
                    Socket socket;
                    while ((socket = serverSocket.accept()) != null && isRunning) {
                        Logger.getInstance().log("wifiserver", "accepted connection");
                        new Thread(new EchoServerHandler(socket.getInputStream(), socket.getOutputStream())).start();
                    }
                } catch (IOException e) {
                    Logger.getInstance().log("wifiserver", "server stopped running: " + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isRunning = false;
    }

    private void makeDiscoverable(){
        WifiP2pManager manager = (WifiP2pManager) this.context.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this.context, this.context.getMainLooper(), null);
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Logger.getInstance().log("wifiserver", "discovery success");
            }

            @Override
            public void onFailure(int reason) {
                Logger.getInstance().log("wifiserver", "discovery error");
            }
        });
    }
}
