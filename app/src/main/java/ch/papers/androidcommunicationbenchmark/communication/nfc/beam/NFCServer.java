package ch.papers.androidcommunicationbenchmark.communication.nfc.beam;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;

import java.util.Arrays;

import ch.papers.androidcommunicationbenchmark.communication.Server;
import ch.papers.androidcommunicationbenchmark.utils.Preferences;


/**
 * Created by Alessandro De Carli (@a_d_c_) on 05/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class NFCServer implements Server {

    private final static String MIMETYPE = "application/vnd.com.example.android.beam";

    private final Activity activity;
    private final NfcAdapter nfcAdapter;

    public NFCServer(Activity activity) {

        this.activity = activity;
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this.activity);
        this.stop();
    }

    @Override
    public void start() {
        this.nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {
            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                byte[] payload = new byte[Preferences.getInstance().getPayloadSize()];
                Arrays.fill(payload, (byte) 1);
                return new NdefMessage(
                        new NdefRecord[] { NdefRecord.createMime(
                                MIMETYPE, payload)
                        });
            }
        },this.activity);
    }

    @Override
    public void stop() {
        if(nfcAdapter!=null) {
            nfcAdapter.setNdefPushMessage(null, this.activity);
            nfcAdapter.setNdefPushMessageCallback(null, this.activity); // optional
        }
    }
}
