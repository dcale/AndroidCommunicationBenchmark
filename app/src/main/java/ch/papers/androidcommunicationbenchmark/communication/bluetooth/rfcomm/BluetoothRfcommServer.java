package ch.papers.androidcommunicationbenchmark.communication.bluetooth.rfcomm;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.communication.EchoServerHandler;
import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */


public class BluetoothRfcommServer implements Server {



    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final Context context;


    private boolean isRunning = false;
    private BluetoothServerSocket serverSocket;

    public BluetoothRfcommServer(Context context) {
        this.context = context;
    }


    @Override
    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(!isRunning) {
                    isRunning = true;
                    try {
                        makeDiscoverable();
                        serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.BROADCAST_NAME, Constants.SERVICE_UUID);
                        BluetoothSocket socket;
                        while ((socket = serverSocket.accept()) != null && isRunning) {
                            Logger.getInstance().log("blueserver", "accepted connection");
                            new Thread(new EchoServerHandler(socket.getInputStream(), socket.getOutputStream())).start();
                        }
                    } catch (IOException e) {
                        Logger.getInstance().log("blueserver", "server stopped running: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    @Override
    public void stop() {
        if(this.isRunning) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.isRunning = false;
        }
    }

    private void makeDiscoverable() {
        Intent discoverableIntent = new
                Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, Constants.DISCOVERABLE_DURATION);
        this.context.startActivity(discoverableIntent);
    }
}

