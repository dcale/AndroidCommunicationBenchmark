package ch.papers.androidcommunicationbenchmark.models;

import android.os.Build;

import ch.papers.objectstorage.models.AbstractUuidObject;


/**
 * Created by Alessandro De Carli (@a_d_c_) on 21/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class BenchmarkResult extends AbstractUuidObject {
    public class ConnectionTechonology {
        public final static short BLUETOOTH_RFCOMM = 1;
        public final static short WIFI = 2;
        public final static short NFC = 3;
        public final static short BLUETOOTH_LE = 4;
    }

    private final long androidVersion = android.os.Build.VERSION.SDK_INT;


    private final String deviceInfo = Build.MANUFACTURER + "/" + Build.MODEL;

    private final short connectionTechnology;

    private final long payloadSize;
    private final long bufferSize;

    private final long discoveryTime;
    private final long connectionTime;
    private final long transferTime;



    private final long timestamp = System.currentTimeMillis();


    public BenchmarkResult(short connectionTechnology, long payloadSize, long bufferSize, long discoveryTime, long connectionTime, long transferTime) {
        this.connectionTechnology = connectionTechnology;
        this.payloadSize = payloadSize;
        this.bufferSize = bufferSize;
        this.discoveryTime = discoveryTime;
        this.connectionTime = connectionTime;
        this.transferTime = transferTime;

    }

    public short getConnectionTechnology() {
        return connectionTechnology;
    }

    public long getPayloadSize() {
        return payloadSize;
    }

    public long getBufferSize() {
        return bufferSize;
    }

    public long getDiscoveryTime() {
        return discoveryTime;
    }

    public long getConnectionTime() {
        return connectionTime;
    }

    public long getTransferTime() {
        return transferTime;
    }

    public double latency() {
        return (this.connectionTime + this.discoveryTime)/1000.0;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getAndroidVersion() {
        return androidVersion;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * gives the bandwith in byte/milisecond
     *
     * @return
     */
    public double bandwidth() {
        return ((this.payloadSize / 1024.0) / (this.transferTime * 1.0))*1000.0;
    }
}
