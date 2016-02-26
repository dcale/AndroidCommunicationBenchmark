package ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import ch.papers.androidcommunicationbenchmark.communication.AbstractServer;
import ch.papers.androidcommunicationbenchmark.communication.EchoServerHandler;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */


public class BluetoothRfcommServer extends AbstractServer {

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final static String TAG = "blueserverRFCOMM";

    private BluetoothServerSocket serverSocket;

    public BluetoothRfcommServer(Context context) {
        super(context);
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void start() {
        this.makeDiscoverable();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isRunning()) {
                    setRunning(true);
                    try {
                        serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.BROADCAST_NAME, Constants.SERVICE_UUID);
                        BluetoothSocket socket;
                        while ((socket = serverSocket.accept()) != null && isRunning()) {
                            Logger.getInstance().log(TAG, "accepted connection");
                            new Thread(new EchoServerHandler(socket.getInputStream(), socket.getOutputStream())).start();
                        }
                    } catch (IOException e) {
                        Logger.getInstance().log(TAG, "server stopped running: " + e.getMessage());
                    }
                    setRunning(false);
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        if (this.isRunning()) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                Logger.getInstance().log(TAG, "server stopped running: " + e.getMessage());
            }
            this.setRunning(false);
        }
    }

    private void makeDiscoverable() {
        if (!this.bluetoothAdapter.isEnabled()) {
            if (this.bluetoothAdapter.enable()) {
                Logger.getInstance().log(TAG, "enabling blueooth");
            } else {
                Logger.getInstance().log(TAG, "enabling not possible at the moment");
            }
        }

        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Constants.DISCOVERABLE_DURATION);
        this.getContext().startActivity(discoverableIntent);
    }
}

