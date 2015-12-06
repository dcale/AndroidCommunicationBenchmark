package ch.papers.androidcommunicationbenchmark.communication.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import ch.papers.androidcommunicationbenchmark.communication.Client;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.communication.EchoClientHandler;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.androidcommunicationbenchmark.utils.Preferences;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class WifiClient implements Client {
    private final Context context;
    private OnResultListener<BenchmarkResult> benchmarkOnResultListener;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private long startTime = 0;

    private boolean isConnected = false;

    private final Map<String, Long> discoveryTimes = new HashMap<String, Long>();
    private final Map<String, Long> connectTimes = new HashMap<String, Long>();
    private final Map<String, Long> transferTimes = new HashMap<String, Long>();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                if(!isConnected) {
                    manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList peers) {
                            for (final WifiP2pDevice peer : peers.getDeviceList()) {
                                WifiP2pConfig config = new WifiP2pConfig();
                                config.deviceAddress = peer.deviceAddress;
                                Logger.getInstance().log("wifiserver", "connecting to peer " + peer.deviceAddress);
                                discoveryTimes.put(peer.deviceAddress, System.currentTimeMillis());
                                manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        //Logger.getInstance().log("wifiserver", "connection succeeded");
                                        manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                                            @Override
                                            public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                                                if (!info.isGroupOwner && info.groupFormed && !isConnected) {
                                                    isConnected = true;
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            connect(peer, info.groupOwnerAddress);
                                                        }
                                                    }).start();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Logger.getInstance().log("wifiserver", "connection failed");
                                    }
                                });
                            }
                        }
                    });
                }
            }
            /*else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                final NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                        Logger.getInstance().log("wifiserver", "connection info is groupowner:" + info.isGroupOwner);
                        Logger.getInstance().log("wifiserver", "connection info ip:" + info.groupOwnerAddress);
                        if (!info.isGroupOwner && info.groupFormed) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    connect(info.,info.groupOwnerAddress);
                                }
                            }).start();
                        }
                    }
                });*/
        }
    };

    public WifiClient(Context context) {
        this.context = context;
    }


    public void connect(WifiP2pDevice peer, InetAddress server) {

        try {
            Socket socket = new Socket(server, Constants.SERVER_PORT);
            Logger.getInstance().log("wifiserver", "connection was successful");
            this.connectTimes.put(peer.deviceAddress, System.currentTimeMillis());
            new EchoClientHandler(socket.getInputStream(), socket.getOutputStream()).run();
            this.transferTimes.put(peer.deviceAddress, System.currentTimeMillis());

            final long discoveryTime = this.discoveryTimes.get(peer.deviceAddress) - this.startTime;
            final long connectTime = this.connectTimes.get(peer.deviceAddress) - discoveryTime - this.startTime;
            final long transferTime = this.transferTimes.get(peer.deviceAddress) - connectTime - discoveryTime - this.startTime;

            this.benchmarkOnResultListener.onSuccess(new BenchmarkResult(BenchmarkResult.ConnectionTechonology.WIFI,
                    Preferences.getInstance().getPayloadSize()* Preferences.getInstance().getCycleCount(), Preferences.getInstance().getPayloadSize(),
                    discoveryTime, connectTime, transferTime));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startBenchmark(OnResultListener<BenchmarkResult> benchmarkOnResultListener) {
        this.benchmarkOnResultListener = benchmarkOnResultListener;


        manager = (WifiP2pManager) this.context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this.context, this.context.getMainLooper(), null);

        IntentFilter filter = new IntentFilter();
        //filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        //filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        //filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        this.context.registerReceiver(this.broadcastReceiver, filter);

        this.startTime = System.currentTimeMillis();
        this.manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
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

