package ch.papers.androidcommunicationbenchmark.communication.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.pm.PackageManager;

import java.util.Arrays;
import java.util.UUID;

import ch.papers.androidcommunicationbenchmark.communication.AbstractClient;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.androidcommunicationbenchmark.utils.Preferences;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BluetoothLEClient extends AbstractClient {
    private final static String TAG = "blueclientLE";

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final static int MAX_MTU = 300;
    private final static int MAX_FRAGMENT_SIZE = MAX_MTU - 2;
    private boolean isRunning = false;

    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            bluetoothAdapter.stopLeScan(this);
            getDiscoveryTimes().put(device.getAddress(), System.currentTimeMillis());

            device.connectGatt(getContext(), false, new BluetoothGattCallback() {
                byte[] payload = new byte[Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount()];
                int byteCounter = 0;

                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);
                    Arrays.fill(payload, (byte) 1);

                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        gatt.requestMtu(MAX_MTU);
                    }

                    Logger.getInstance().log(TAG, gatt.getDevice().getAddress() + " changed connection state");
                }

                @Override
                public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                    super.onMtuChanged(gatt, mtu, status);
                    gatt.discoverServices();
                    Logger.getInstance().log(TAG, gatt.getDevice().getAddress() + " changed connection state");
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Logger.getInstance().log(TAG, "discovered service:" + status);
                    getConnectTimes().put(gatt.getDevice().getAddress(), System.currentTimeMillis());
                    BluetoothGattCharacteristic characteristic = gatt.getService(Constants.SERVICE_UUID).getCharacteristic(Constants.CHARACTERISTIC_UUID);

                    Logger.getInstance().log(TAG, "got characteristics:" + characteristic.getPermissions());

                    byte[] fragment = Arrays.copyOfRange(payload, byteCounter, byteCounter + MAX_FRAGMENT_SIZE);
                    Logger.getInstance().log(TAG, "sending bytes: " + fragment.length);

                    characteristic.setValue(fragment);
                    gatt.writeCharacteristic(characteristic);
                    byteCounter += fragment.length;

                }

                // onMtuchanged -> send service!!!


                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    Logger.getInstance().log(TAG, "read receiving bytes: " + characteristic.getValue().length);
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Logger.getInstance().log(TAG, gatt.getDevice().getAddress() + " wrote characteristic:" + status);
                    Logger.getInstance().log(TAG, "write receiving bytes: " + characteristic.getValue().length);

                    if (byteCounter < payload.length && isRunning) {
                        byte[] payload = new byte[Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount()];
                        Arrays.fill(payload, (byte) 1);

                        byte[] fragment = Arrays.copyOfRange(payload, 0, MAX_FRAGMENT_SIZE);
                        Logger.getInstance().log(TAG, "sending bytes: " + fragment.length);

                        characteristic.setValue(fragment);
                        gatt.writeCharacteristic(characteristic);
                        byteCounter += fragment.length;
                    } else {
                        getTransferTimes().put(gatt.getDevice().getAddress(), System.currentTimeMillis());
                        endBenchmark(gatt.getDevice().getAddress());
                    }
                }
            });

        }
    };


    public BluetoothLEClient(Context context) {
        super(context);
    }

    @Override
    public boolean isSupported() {
        return this.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @Override
    public void stop() {
        bluetoothAdapter.stopLeScan(this.leScanCallback);
        this.isRunning = false;
    }

    @Override
    public short getConnectionTechnology() {
        return BenchmarkResult.ConnectionTechonology.BLUETOOTH_LE;
    }

    @Override
    protected void startBenchmark() {
        this.isRunning = true;
        if (!this.bluetoothAdapter.isEnabled()) {
            if (this.bluetoothAdapter.enable()) {
                Logger.getInstance().log(TAG, "enabling blueooth");
            } else {
                Logger.getInstance().log(TAG, "enabling not possible at the moment");
            }
        }

        UUID[] services = {Constants.SERVICE_UUID};
        bluetoothAdapter.startLeScan(services, this.leScanCallback);
    }
}
