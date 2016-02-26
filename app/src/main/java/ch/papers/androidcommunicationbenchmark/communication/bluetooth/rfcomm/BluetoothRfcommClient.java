package ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;

import ch.papers.androidcommunicationbenchmark.communication.AbstractClient;
import ch.papers.androidcommunicationbenchmark.communication.EchoClientHandler;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BluetoothRfcommClient extends AbstractClient{
    private final static String TAG = "blueclientRFCOMM";
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private BluetoothSocket socket;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Logger.getInstance().log(TAG, "found new device '" + device.getAddress() + "', connecting");
                try {
                    getDiscoveryTimes().put(device.getAddress(), System.currentTimeMillis());
                    connect(device);
                } catch (Exception e) {
                }
            }
        }
    };

    public BluetoothRfcommClient(Context context) {
        super(context);
    }

    @Override
    public short getConnectionTechnology() {
        return BenchmarkResult.ConnectionTechonology.BLUETOOTH_RFCOMM;
    }

    @Override
    protected void startBenchmark() {
        if (!this.bluetoothAdapter.isEnabled()) {
            if (this.bluetoothAdapter.enable()) {
                Logger.getInstance().log(TAG, "enabling blueooth");
            } else {
                Logger.getInstance().log(TAG, "enabling not possible at the moment");
            }
        }

        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.getContext().registerReceiver(this.broadcastReceiver, filter);
        this.bluetoothAdapter.startDiscovery();
    }

    public void connect(BluetoothDevice server) {

        try {
            BluetoothSocket socket = server.createInsecureRfcommSocketToServiceRecord(Constants.SERVICE_UUID);
            socket.connect();

            Logger.getInstance().log(TAG, "connection was successful");
            this.endDiscovery();

            this.getConnectTimes().put(server.getAddress(), System.currentTimeMillis());
            new EchoClientHandler(socket.getInputStream(), socket.getOutputStream()).run();
            this.getTransferTimes().put(server.getAddress(), System.currentTimeMillis());

            this.endBenchmark(server.getAddress());
            socket.close();
        } catch (IOException e) {
            Logger.getInstance().log(TAG, "couldn't connect to "+server.getAddress()+": "+e.getMessage());
        }

    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void stop() {
        this.endDiscovery();
        if(this.socket !=null) {
            try {
                this.socket.close();
            } catch (IOException e) {}
        }
        Logger.getInstance().log(TAG, "client stopped");
    }

    private void endDiscovery(){
        this.bluetoothAdapter.cancelDiscovery();
        try {
            this.getContext().unregisterReceiver(this.broadcastReceiver);
        } catch (Exception e){

        }
    }

}
