package ch.papers.androidcommunicationbenchmark.communication.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import ch.papers.androidcommunicationbenchmark.communication.AbstractClient;
import ch.papers.androidcommunicationbenchmark.communication.EchoClientHandler;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class WifiClient extends AbstractClient {
    private final static String TAG = "wificlient";

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiP2pDevice currentPeer;
    private Socket socket;

    private boolean isConnected = false;

    private final Runnable connectionHandler = new Runnable() {
        @Override
        public void run() {
            if (!isConnected) {
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        for (final WifiP2pDevice peer : peers.getDeviceList()) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = peer.deviceAddress;
                            Logger.getInstance().log(TAG, "connecting to peer " + peer.deviceAddress);
                            getDiscoveryTimes().put(peer.deviceAddress, System.currentTimeMillis());
                            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {

                                    manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                                        @Override
                                        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                                            currentPeer = peer;
                                            if (!info.isGroupOwner && info.groupFormed && !isConnected) {
                                                isConnected = true;
                                                new Thread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        connect(info.groupOwnerAddress);
                                                    }
                                                }).start();
                                            } else if (info.isGroupOwner && info.groupFormed && !isConnected) {
                                                manager.removeGroup(channel,actionListener);
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(int reason) {

                                    Logger.getInstance().log(TAG, "connection failed " + reason);
                                }
                            });
                        }
                    }
                });
            }
        }
    };

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

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                new Thread(connectionHandler).start();
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                new Thread(connectionHandler).start();
            }
        }
    };

    public WifiClient(Context context) {
        super(context);
    }


    public void connect(InetAddress server) {
        if(this.currentPeer != null) {

            try {
                socket = new Socket(server, Constants.ECHO_SERVER_PORT);
                this.stopDiscovery();

                Logger.getInstance().log(TAG, "connection was successful");
                this.getConnectTimes().put(this.currentPeer.deviceAddress, System.currentTimeMillis());
                new EchoClientHandler(socket.getInputStream(), socket.getOutputStream()).run();
                this.getTransferTimes().put(this.currentPeer.deviceAddress, System.currentTimeMillis());

                this.endBenchmark(this.currentPeer.deviceAddress);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
    public void stop() {
        this.stopDiscovery();
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    private void stopDiscovery() {
        this.manager.stopPeerDiscovery(channel, actionListener);
        try {
            this.getContext().unregisterReceiver(this.broadcastReceiver);
        } catch (Exception e) {

        }
    }

    @Override
    public short getConnectionTechnology() {
        return BenchmarkResult.ConnectionTechonology.WIFI;
    }

    @Override
    protected void startBenchmark() {
        WifiManager wifiManager = (WifiManager)this.getContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);


        manager = (WifiP2pManager) this.getContext().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this.getContext(), this.getContext().getMainLooper(), null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        this.getContext().registerReceiver(this.broadcastReceiver, filter);

        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                manager.discoverPeers(channel, actionListener);
            }

            @Override
            public void onFailure(int reason) {
                manager.discoverPeers(channel, actionListener);
            }
        });




    }
}

