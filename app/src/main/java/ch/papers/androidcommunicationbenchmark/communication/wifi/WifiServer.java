package ch.papers.androidcommunicationbenchmark.communication.wifi;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ch.papers.androidcommunicationbenchmark.communication.AbstractServer;
import ch.papers.androidcommunicationbenchmark.communication.EchoServerHandler;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class WifiServer extends AbstractServer {
    private final static String TAG = "wifiserver";

    private ServerSocket serverSocket;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;

    private final WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() {
        @Override
        public void onSuccess() {
            Logger.getInstance().log(TAG, "discovery success");
        }

        @Override
        public void onFailure(int reason) {
            Logger.getInstance().log(TAG, "discovery error");
        }
    };

    public WifiServer(Context context) {
        super(context);
    }

    @Override
    public boolean isSupported() {
        // see http://stackoverflow.com/questions/23828487/how-can-i-check-my-android-device-support-wifi-direct
        PackageManager pm = this.getContext().getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo info : features) {
            if (info != null && info.name != null && info.name.equalsIgnoreCase("android.hardware.wifi.direct")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        WifiManager wifiManager = (WifiManager)this.getContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                setRunning(true);
                try {
                    makeDiscoverable();
                    serverSocket = new ServerSocket(Constants.ECHO_SERVER_PORT);
                    Socket socket;
                    while ((socket = serverSocket.accept()) != null && isRunning()) {
                        Logger.getInstance().log(TAG, "accepted connection");
                        new Thread(new EchoServerHandler(socket.getInputStream(), socket.getOutputStream())).start();
                    }
                } catch (IOException e) {
                    Logger.getInstance().log(TAG, "server stopped running: " + e.getMessage());
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

        this.manager.stopPeerDiscovery(channel, actionListener);
        this.manager.removeGroup(channel,actionListener);
        this.setRunning(false);
    }

    private void makeDiscoverable() {
        this.manager = (WifiP2pManager) this.getContext().getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = this.manager.initialize(this.getContext(), this.getContext().getMainLooper(), null);
        this.manager.createGroup(channel, actionListener);
    }
}
