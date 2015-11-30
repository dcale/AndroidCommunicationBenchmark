package ch.papers.androidcommunicationbenchmark.communication.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import ch.papers.androidcommunicationbenchmark.communication.Constants;
import ch.papers.androidcommunicationbenchmark.communication.EchoServerHandler;
import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 15/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */


public class BluetoothServer implements Server {


    private boolean isRunning = false;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    @Override
    public void start() {
        this.isRunning=true;
        try {
            BluetoothServerSocket serverSocket = this.bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(Constants.BROADCAST_NAME, Constants.SERVICE_UUID);
            BluetoothSocket socket;
            while ((socket = serverSocket.accept()) != null && this.isRunning) {
                Logger.getInstance().log("blueserver","accepted connection");
                new Thread(new EchoServerHandler(socket.getInputStream(),socket.getOutputStream())).start();
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
