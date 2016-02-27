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
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import ch.papers.androidcommunicationbenchmark.communication.AbstractServer;
import ch.papers.androidcommunicationbenchmark.utils.Constants;
import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BluetoothLEServer extends AbstractServer {
    private final static String TAG = "blueserverLE";
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGattServer bluetoothGattServer;
    private byte[] value;
    private int offset;

    public BluetoothLEServer(Context context) {
        super(context);
    }

    @Override
    public boolean isSupported() {
        // see http://stackoverflow.com/questions/26482611/chipsets-devices-supporting-android-5-ble-peripheral-mode
        return this.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) &&
        bluetoothAdapter.isMultipleAdvertisementSupported() &&
        bluetoothAdapter.isOffloadedFilteringSupported() &&
        bluetoothAdapter.isOffloadedScanBatchingSupported();
    }

    @Override
    public void start() {
        if(!this.isRunning() && this.isSupported()) {
            this.setRunning(true);

            final BluetoothManager bluetoothManager =
                    (BluetoothManager) this.getContext().getSystemService(Context.BLUETOOTH_SERVICE);

            this.bluetoothGattServer = bluetoothManager.openGattServer(this.getContext(), new BluetoothGattServerCallback() {
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                    super.onConnectionStateChange(device, status, newState);


                    switch (newState){
                        case BluetoothGatt.STATE_CONNECTING:
                            Logger.getInstance().log(TAG, device.getAddress() + " changed connection state to connecting");
                            break;
                        case BluetoothGatt.STATE_CONNECTED:
                            Logger.getInstance().log(TAG, device.getAddress() + " changed connection state to connected");
                            break;
                        case BluetoothGatt.STATE_DISCONNECTING:
                            Logger.getInstance().log(TAG, device.getAddress() + " changed connection state to disconnecting");
                            break;
                        case BluetoothGatt.STATE_DISCONNECTED:
                            Logger.getInstance().log(TAG, device.getAddress() + " changed connection state to disconnected");
                            break;
                        default:
                            Logger.getInstance().log(TAG, device.getAddress() + " changed connection state to "+newState);
                            break;
                    }

                }

                @Override
                public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                    Logger.getInstance().log(TAG, device.getAddress() + " requested characteristic read");
                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
                }

                @Override
                public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                    super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                    Logger.getInstance().log(TAG, device.getAddress() + " requested characteristic write with " + value.length + " payload");
                    BluetoothLEServer.this.value = value;
                    BluetoothLEServer.this.offset = offset;
                    //bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, new byte[]{1});
                }

            });



            BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(Constants.WRITE_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, BluetoothGattCharacteristic.PERMISSION_WRITE);
            BluetoothGattCharacteristic readCharacteristic = new BluetoothGattCharacteristic(Constants.READ_CHARACTERISTIC_UUID, BluetoothGattCharacteristic.PROPERTY_READ, BluetoothGattCharacteristic.PERMISSION_READ);
            BluetoothGattService service = new BluetoothGattService(Constants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
            service.addCharacteristic(writeCharacteristic);
            service.addCharacteristic(readCharacteristic);


            bluetoothGattServer.addService(service);
            bluetoothAdapter.getBluetoothLeAdvertiser().startAdvertising(new AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                            .setConnectable(true).setTimeout(0)
                            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH).build(),
                    new AdvertiseData.Builder().setIncludeDeviceName(true).addServiceUuid(ParcelUuid.fromString(Constants.SERVICE_UUID.toString())).build(), new AdvertiseCallback() {
                    });
        }
    }

    @Override
    public void stop() {
        this.bluetoothGattServer.clearServices();
        this.bluetoothGattServer.close();
        this.setRunning(false);
    }
}
