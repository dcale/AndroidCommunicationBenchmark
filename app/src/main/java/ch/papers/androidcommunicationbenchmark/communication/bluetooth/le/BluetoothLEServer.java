package ch.papers.androidcommunicationbenchmark.communication.bluetooth.le;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.os.ParcelUuid;

import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BluetoothLEServer implements Server {

    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private final Context context;


    private BluetoothGattServer bluetoothGattServer;


    public BluetoothLEServer(Context context) {
        this.context = context;
    }

    @Override
    public void start() {

        final BluetoothManager bluetoothManager =
                (BluetoothManager) this.context.getSystemService(Context.BLUETOOTH_SERVICE);

        this.bluetoothGattServer = bluetoothManager.openGattServer(this.context, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                Logger.getInstance().log("bleserver", device.getAddress() + " changed connection state");
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                Logger.getInstance().log("bleserver", device.getAddress() + " requested characteristic read");
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device,requestId,characteristic,preparedWrite,responseNeeded,offset,value);
                Logger.getInstance().log("bleserver", device.getAddress() + " requested characteristic write with " + value.length + " payload");
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
            }

        });

        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(Constants.CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
        BluetoothGattService service = new BluetoothGattService(Constants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        service.addCharacteristic(characteristic);


        bluetoothGattServer.addService(service);
        bluetoothAdapter.getBluetoothLeAdvertiser().startAdvertising(new AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                        .setConnectable(true).setTimeout(0)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).build(),
                new AdvertiseData.Builder().setIncludeDeviceName(true).addServiceUuid(ParcelUuid.fromString(Constants.SERVICE_UUID.toString())).build(), new AdvertiseCallback() {

                });
    }

    @Override
    public void stop() {
        this.bluetoothGattServer.clearServices();
        this.bluetoothGattServer.close();
    }
}
