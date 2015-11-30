package ch.papers.androidcommunicationbenchmark.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class EchoServerHandler implements Runnable {

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public EchoServerHandler(InputStream inputstream, OutputStream outputStream) {
        this.inputStream = inputstream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[Constants.DEFAULT_BUFFER_SIZE];
            int readBytes = 0;
            while ((readBytes = this.inputStream.read(buffer)) > 0) {
                Logger.getInstance().log("server","read bytes: "+readBytes);
                this.outputStream.write(buffer, 0, readBytes);
                this.outputStream.flush();
                if (new String(buffer, 0, readBytes).equals("CLOSE")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
