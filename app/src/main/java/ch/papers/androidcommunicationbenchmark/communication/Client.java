package ch.papers.androidcommunicationbenchmark.communication;

import ch.papers.androidcommunicationbenchmark.models.BenchmarkResult;
import ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners.OnResultListener;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 04/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public interface Client {
    public void startBenchmark(OnResultListener<BenchmarkResult> benchmarkOnResultListener);
}
