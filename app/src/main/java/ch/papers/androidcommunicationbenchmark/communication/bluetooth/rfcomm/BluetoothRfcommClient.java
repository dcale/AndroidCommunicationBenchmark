package ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
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
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BluetoothRfcommClient implements Client{
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private final Map<String, Long> discoveryTimes = new HashMap<String, Long>();
    private final Map<String, Long> connectTimes = new HashMap<String, Long>();
    private final Map<String, Long> transferTimes = new HashMap<String, Long>();

    private OnResultListener<BenchmarkResult> benchmarkOnResultListener;

    private final Context context;


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Logger.getInstance().log("broadcastreceiver", "found new device '" + device.getAddress() + "', connecting");
                try {
                    discoveryTimes.put(device.getAddress(), System.currentTimeMillis());
                    connect(device);
                } catch (Exception e) {
                }
            }
        }
    };


    private long startTime = 0;

    public BluetoothRfcommClient(Context context) {
        this.context = context;
    }

    public void connect(BluetoothDevice server) {

        try {
            BluetoothSocket socket = server.createInsecureRfcommSocketToServiceRecord(Constants.SERVICE_UUID);
            socket.connect();
            this.context.unregisterReceiver(this.broadcastReceiver);
            Logger.getInstance().log("blueclient", "connection was successful");
            this.connectTimes.put(server.getAddress(), System.currentTimeMillis());
            new EchoClientHandler(socket.getInputStream(), socket.getOutputStream()).run();
            this.transferTimes.put(server.getAddress(), System.currentTimeMillis());

            final long discoveryTime = this.discoveryTimes.get(server.getAddress()) - this.startTime;
            final long connectTime = this.connectTimes.get(server.getAddress()) - discoveryTime - this.startTime;
            final long transferTime = this.transferTimes.get(server.getAddress()) - connectTime - discoveryTime - this.startTime;

            this.benchmarkOnResultListener.onSuccess(new BenchmarkResult(BenchmarkResult.ConnectionTechonology.BLUETOOTH_RFCOMM,
                    Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount(), Preferences.getInstance().getPayloadSize(),
                    discoveryTime, connectTime,  transferTime));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void startBenchmark(OnResultListener<BenchmarkResult> benchmarkOnResultListener) {
        this.benchmarkOnResultListener = benchmarkOnResultListener;

        this.startTime = System.currentTimeMillis();
        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        this.context.registerReceiver(this.broadcastReceiver, filter);
        this.bluetoothAdapter.startDiscovery();
    }
}
