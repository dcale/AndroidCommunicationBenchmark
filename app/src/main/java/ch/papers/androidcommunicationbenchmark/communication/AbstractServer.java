package ch.papers.androidcommunicationbenchmark.communication;

import android.content.Context;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 02/01/16.
 * Papers.ch
 * a.decarli@papers.ch
 */
public abstract class AbstractServer implements Server {
    private final Context context;
    private boolean isRunning = false;

    public AbstractServer(Context context){
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
