package ch.papers.androidcommunicationbenchmark.communication.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
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

import ch.papers.androidcommunicationbenchmark.communication.Constants;
import ch.papers.androidcommunicationbenchmark.communication.EchoClientHandler;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class WifiClient {
    private OnResultListener<BenchmarkResult> benchmarkOnResultListener;
    private Activity activity;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private long startTime = 0;

    private final Map<String, Long> discoveryTimes = new HashMap<String, Long>();
    private final Map<String, Long> connectTimes = new HashMap<String, Long>();
    private final Map<String, Long> transferTimes = new HashMap<String, Long>();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        for(WifiP2pDevice peer: peers.getDeviceList()){

                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = peer.deviceAddress;
                            Logger.getInstance().log("wifiserver", "connecting to peer "+peer.deviceAddress);
                            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess() {
                                    Logger.getInstance().log("wifiserver", "connection succeeded");
                                    manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                                        @Override
                                        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                                            Logger.getInstance().log("wifiserver", "connection info"+info);
                                            if(!info.isGroupOwner && info.groupFormed){
                                                new Thread(new Runnable(){
                                                    @Override
                                                    public void run() {
                                                        connect(info.groupOwnerAddress);
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
            } else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                manager.requestConnectionInfo(channel, new WifiP2pManager.ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                        Logger.getInstance().log("wifiserver", "connection info is groupowner:"+info.isGroupOwner);
                        Logger.getInstance().log("wifiserver", "connection info ip:"+info.groupOwnerAddress);
                        if(!info.isGroupOwner && info.groupFormed){
                            new Thread(new Runnable(){
                                @Override
                                public void run() {
                                    connect(info.groupOwnerAddress);
                                }
                            }).start();
                        }
                    }
                });
            }
        }
    };


    public void startBenchmark(Activity activity, OnResultListener<BenchmarkResult> benchmarkOnResultListener) {
        this.benchmarkOnResultListener = benchmarkOnResultListener;
        this.activity = activity;


        manager = (WifiP2pManager) this.activity.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this.activity, this.activity.getMainLooper(), null);

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        this.activity.registerReceiver(this.broadcastReceiver, filter);

        this.startTime = System.currentTimeMillis();
        this.manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Logger.getInstance().log("wifiserver","discovery success");
            }

            @Override
            public void onFailure(int reason) {
                Logger.getInstance().log("wifiserver","discovery error");
            }
        });
    }


    public void connect(InetAddress server) {

        try {
            Socket socket = new Socket(server,Constants.SERVER_PORT);
            this.activity.unregisterReceiver(this.broadcastReceiver);
            Logger.getInstance().log("wifiserver", "connection was successful");
            this.connectTimes.put(server.getHostAddress(), System.currentTimeMillis());
            new EchoClientHandler(socket.getInputStream(), socket.getOutputStream()).run();
            this.transferTimes.put(server.getHostAddress(), System.currentTimeMillis());

            final long discoveryTime = this.discoveryTimes.get(server.getAddress()) - this.startTime;
            final long connectTime = this.connectTimes.get(server.getAddress()) - discoveryTime - this.startTime;
            final long transferTime = this.transferTimes.get(server.getAddress()) - connectTime - this.startTime;

            this.benchmarkOnResultListener.onSuccess(new BenchmarkResult(BenchmarkResult.ConnectionTechonology.BLUETOOTH,
                    Constants.DEFAULT_BUFFER_SIZE * Constants.CYCLE_COUNT, Constants.DEFAULT_BUFFER_SIZE,
                    discoveryTime, connectTime,  transferTime));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
