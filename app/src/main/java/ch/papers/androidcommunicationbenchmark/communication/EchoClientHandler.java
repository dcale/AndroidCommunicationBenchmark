package ch.papers.androidcommunicationbenchmark.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.papers.androidcommunicationbenchmark.utils.Logger;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 30/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class EchoClientHandler implements Runnable {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    private final List<Long> elapsedTimes = new ArrayList<Long>();

    public EchoClientHandler(InputStream inputstream, OutputStream outputStream) {
        this.inputStream = inputstream;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        try {
            byte[] payload = new byte[Constants.DEFAULT_BUFFER_SIZE];
            Arrays.fill(payload, (byte) 1);

            long startTime = System.currentTimeMillis();
            for (int i = 0; i < Constants.CYCLE_COUNT; i++) {
                Logger.getInstance().log("client", "starting cycle: " + i);
                this.outputStream.write(payload);
                this.outputStream.flush();
                Logger.getInstance().log("client", "payload written");
                byte[] buffer = new byte[Constants.DEFAULT_BUFFER_SIZE];
                int totalBytes = 0;
                while ((totalBytes += this.inputStream.read(buffer)) > 0 && totalBytes < payload.length) {
                    Logger.getInstance().log("client", "total bytes: " + totalBytes);
                }
                ;

                this.elapsedTimes.add(System.currentTimeMillis() - startTime);
                Logger.getInstance().log("client", "elapsed time: " + this.elapsedTimes.get(this.elapsedTimes.size() - 1));
                startTime = System.currentTimeMillis();
            }

            this.outputStream.write("CLOSE".getBytes());
            this.inputStream.close();
            this.outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
