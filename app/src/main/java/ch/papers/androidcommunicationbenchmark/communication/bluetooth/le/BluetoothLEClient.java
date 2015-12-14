package ch.papers.androidcommunicationbenchmark.communication.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import ch.papers.androidcommunicationbenchmark.communication.Client;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.androidcommunicationbenchmark.utils.Preferences;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BluetoothLEClient implements Client {

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private long startTime = 0;
    private final Context context;
    private final static int MAX_MTU = 20;
    private final static int MAX_FRAGMENT_SIZE = MAX_MTU - 2;

    private OnResultListener<BenchmarkResult> benchmarkOnResultListener;


    private final Map<String, Long> discoveryTimes = new HashMap<String, Long>();
    private final Map<String, Long> connectTimes = new HashMap<String, Long>();
    private final Map<String, Long> transferTimes = new HashMap<String, Long>();

    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            bluetoothAdapter.stopLeScan(this);
            discoveryTimes.put(device.getAddress(), System.currentTimeMillis());
            device.connectGatt(context, false, new BluetoothGattCallback() {
                byte[] payload = new byte[Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount()];
                int byteCounter = 0;

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    Arrays.fill(payload, (byte) 1);
                    //gatt.requestMtu(MAX_MTU);
                    gatt.discoverServices();
                    Logger.getInstance().log("bleclient", gatt.getDevice().getAddress() + " changed connection state");

                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Logger.getInstance().log("bleclient", "discovered service:" + status);
                    connectTimes.put(gatt.getDevice().getAddress(), System.currentTimeMillis());
                    BluetoothGattCharacteristic characteristic = gatt.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.CHARACTERISTIC_UUID);

                    Logger.getInstance().log("bleclient", "got characteristics:" + characteristic.getPermissions());

                    byte[] fragment = Arrays.copyOfRange(payload, byteCounter, byteCounter + MAX_FRAGMENT_SIZE);
                    Logger.getInstance().log("bleclient", "sending bytes: " + fragment.length);

                    characteristic.setValue(fragment);
                    gatt.writeCharacteristic(characteristic);
                    byteCounter += fragment.length;

                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Logger.getInstance().log("bleclient", gatt.getDevice().getAddress() + " wrote characteristic:" + status);
                    Logger.getInstance().log("bleclient", "receiving bytes: " + characteristic.getValue().length);

                    if (byteCounter < payload.length) {
                        byte[] payload = new byte[Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount()];
                        Arrays.fill(payload, (byte) 1);

                        byte[] fragment = Arrays.copyOfRange(payload, 0, MAX_FRAGMENT_SIZE);
                        Logger.getInstance().log("bleclient", "sending bytes: " + fragment.length);

                        characteristic.setValue(fragment);
                        gatt.writeCharacteristic(characteristic);
                        byteCounter += fragment.length;
                    } else {
                        transferTimes.put(gatt.getDevice().getAddress(), System.currentTimeMillis());

                        final long discoveryTime = discoveryTimes.get(gatt.getDevice().getAddress()) - startTime;
                        final long connectTime = connectTimes.get(gatt.getDevice().getAddress()) - discoveryTime - startTime;
                        final long transferTime = transferTimes.get(gatt.getDevice().getAddress()) - connectTime - discoveryTime - startTime;

                        benchmarkOnResultListener.onSuccess(new BenchmarkResult(BenchmarkResult.ConnectionTechonology.BLUETOOTH_LE,
                                Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount(), Preferences.getInstance().getPayloadSize(),
                                discoveryTime, connectTime,  transferTime));
                    }
                }


            });
        }
    };


    public BluetoothLEClient(Context context) {

        this.context = context;
    }


    @Override
    public void startBenchmark(OnResultListener<BenchmarkResult> benchmarkOnResultListener) {
        this.benchmarkOnResultListener = benchmarkOnResultListener;
        this.startTime = System.currentTimeMillis();
        UUID[] services = {Constants.SERVICE_UUID};
        bluetoothAdapter.startLeScan(services, this.leScanCallback);
    }
}
