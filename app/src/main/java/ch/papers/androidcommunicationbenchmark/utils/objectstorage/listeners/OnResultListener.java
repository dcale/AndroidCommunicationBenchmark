package ch.papers.androidcommunicationbenchmark.utils.objectstorage.listeners;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 23/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */

public interface OnResultListener<T> {

    public void onSuccess(T result);

    public void onError(String message);
}
