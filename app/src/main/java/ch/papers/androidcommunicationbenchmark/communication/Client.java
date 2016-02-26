package ch.papers.androidcommunicationbenchmark.communication;

import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 04/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public interface Client {
    public boolean isSupported();
    public void start(OnResultListener<BenchmarkResult> benchmarkOnResultListener);
    public void stop();
}
