package ch.papers.androidcommunicationbenchmark.communication.nfc.beam;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 05/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class NFCReceiverActivity extends AppCompatActivity {
    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);
            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) rawMsgs[0];
            msg.getRecords()[0].getPayload();
        }
    }
}
