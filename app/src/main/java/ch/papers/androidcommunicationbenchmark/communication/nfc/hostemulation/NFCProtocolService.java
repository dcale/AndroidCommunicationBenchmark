package ch.papers.androidcommunicationbenchmark.communication.nfc.hostemulation;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class NFCProtocolService extends HostApduService {


    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        if (this.selectAidApdu(commandApdu)) {
            Logger.getInstance().log("nfcservice", "hanshake");
        }
        Logger.getInstance().log("nfcservice", "in transfer " + commandApdu.length + " bytes");
        return commandApdu;
    }

    @Override
    public void onDeactivated(int reason) {
        Logger.getInstance().log("nfcservice", "deactivated");
    }

    private boolean selectAidApdu(byte[] apdu) {
        return apdu.length >= 2 && apdu[0] == (byte) 0 && apdu[1] == (byte) 0xa4;
    }
}
