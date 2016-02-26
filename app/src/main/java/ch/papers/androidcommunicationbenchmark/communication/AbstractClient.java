package ch.papers.androidcommunicationbenchmark.communication;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.Preferences;
import ch.papers.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 02/01/16.
 * Papers.ch
 * a.decarli@papers.ch
 */
public abstract class AbstractClient implements Client {
    private OnResultListener<BenchmarkResult> benchmarkOnResultListener;
    private long startTime = 0;

    private final Map<String, Long> discoveryTimes = new HashMap<String, Long>();
    private final Map<String, Long> connectTimes = new HashMap<String, Long>();
    private final Map<String, Long> transferTimes = new HashMap<String, Long>();

    private final Context context;

    public AbstractClient(Context context) {
        this.context = context;
    }

    public Map<String, Long> getDiscoveryTimes() {
        return discoveryTimes;
    }

    public Map<String, Long> getConnectTimes() {
        return connectTimes;
    }

    public Map<String, Long> getTransferTimes() {
        return transferTimes;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public final void start(OnResultListener<BenchmarkResult> benchmarkOnResultListener) {
        this.benchmarkOnResultListener = benchmarkOnResultListener;
        if(this.isSupported()) {
            this.startTime = System.currentTimeMillis();
            this.startBenchmark();
        } else {
            this.failBenchmark("not supported connection type");
        }
    }

    protected void failBenchmark(String error){
        this.benchmarkOnResultListener.onError(error);
    }

    protected void endBenchmark(String serverIdentifier) {
        final long discoveryTime = getDiscoveryTimes().get(serverIdentifier) - this.startTime;
        final long connectTime = getConnectTimes().get(serverIdentifier) - discoveryTime - this.startTime;
        final long transferTime = getTransferTimes().get(serverIdentifier) - connectTime - discoveryTime - this.startTime;

        benchmarkOnResultListener.onSuccess(new BenchmarkResult(this.getConnectionTechnology(),
                Preferences.getInstance().getPayloadSize() * Preferences.getInstance().getCycleCount(), Preferences.getInstance().getPayloadSize(),
                discoveryTime, connectTime, transferTime));
    }

    public abstract short getConnectionTechnology();
    protected abstract void startBenchmark();
}
