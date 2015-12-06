package ch.papers.androidcommunicationbenchmark.communication.nfc.hostemulation;

import android.app.Activity;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;

import java.io.IOException;
import java.util.Arrays;

import ch.papers.androidcommunicationbenchmark.communication.Client;
import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Logger;
import ch.papers.androidcommunicationbenchmark.utils.Preferences;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class NFCClient implements Client {

    private static final byte[] CLA_INS_P1_P2 = { (byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00 };
    private static final byte[] AID_ANDROID = { (byte)0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };


    private static final byte[] selectCommand = {
            (byte)0x00, // CLA
            (byte)0xA4, // INS
            (byte)0x04, // P1
            (byte)0x00, // P2
            (byte)0x0A, // LC
            (byte)0x01,(byte)0x02,(byte)0x03,(byte)0x04,(byte)0x05,(byte)0x06,(byte)0x07,(byte)0x08,(byte)0x09,(byte)0xFF, // AID
            (byte)0x7F  // LE
    };

    private final Activity activity;
    private final NfcAdapter nfcAdapter;
    private long startTime = 0;

    public NFCClient(Activity activity) {

        this.activity = activity;
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this.activity);
    }

    @Override
    public void startBenchmark(final OnResultListener<BenchmarkResult> benchmarkOnResultListener) {
        this.startTime = System.currentTimeMillis();
        nfcAdapter.enableReaderMode(this.activity, new NfcAdapter.ReaderCallback() {
                    @Override
                    public void onTagDiscovered(Tag tag) {
                        try {
                            final long discoveryTime = System.currentTimeMillis() - startTime;
                            IsoDep isoDep = IsoDep.get(tag);
                            isoDep.connect();

                            byte[] response = isoDep.transceive(createSelectAidApdu(AID_ANDROID));
                            final long connectTime = System.currentTimeMillis() - discoveryTime - startTime;
                            Logger.getInstance().log("nfcclient","is connected "+isoDep.isConnected());
                            Logger.getInstance().log("nfcclient","is extended apdu supported "+isoDep.isExtendedLengthApduSupported());

                            byte[] payload = new byte[Preferences.getInstance().getPayloadSize()];
                            Arrays.fill(payload, (byte) 1);

                            for (int i = 0; i < Preferences.getInstance().getCycleCount(); i++) {
                                int byteCounter = 0;
                                Logger.getInstance().log("nfcclient", "cycle: " + i);
                                while(byteCounter < payload.length){
                                    byte[] fragment = Arrays.copyOfRange(payload,byteCounter,byteCounter+isoDep.getMaxTransceiveLength());
                                    Logger.getInstance().log("nfcclient", "sending bytes: " + fragment.length);
                                    response = isoDep.transceive(fragment);
                                    Logger.getInstance().log("nfcclient", "receiving bytes: " + response.length);
                                    byteCounter += fragment.length;
                                }
                            }

                            isoDep.transceive("CLOSE".getBytes());
                            isoDep.close();
                            final long transferTime = System.currentTimeMillis() - connectTime - discoveryTime - startTime;

                            benchmarkOnResultListener.onSuccess(new BenchmarkResult(BenchmarkResult.ConnectionTechonology.NFC,
                                    Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount(), Preferences.getInstance().getPayloadSize(),
                                    discoveryTime, connectTime,  transferTime));

                        } catch (IOException e) {
                            Logger.getInstance().log("nfcclient", e.getMessage());
                        }
                    }
                }, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null);
    }


    private byte[] createSelectAidApdu(byte[] aid) {
        byte[] result = new byte[6 + aid.length];
        System.arraycopy(CLA_INS_P1_P2, 0, result, 0, CLA_INS_P1_P2.length);
        result[4] = (byte)aid.length;
        System.arraycopy(aid, 0, result, 5, aid.length);
        result[result.length - 1] = 0;
        return result;
    }
}
