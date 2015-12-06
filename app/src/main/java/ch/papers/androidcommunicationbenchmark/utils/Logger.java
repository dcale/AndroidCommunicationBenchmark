package ch.papers.androidcommunicationbenchmark.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 21/11/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class Logger {
    private static final Logger INSTANCE = new Logger();
    private final List<OnLogChangedListener> logChangedListeners = new ArrayList();

    public interface OnLogChangedListener {
        public void onLogChanged(final String tag, final String log);
    }

    private Logger() {

    }

    public static Logger getInstance() {
        return INSTANCE;
    }

    public void registerOnLogChangedListener(OnLogChangedListener onLogChangedListener){
        this.logChangedListeners.add(onLogChangedListener);
    }

    public void unregisterOnLogChangedListener(OnLogChangedListener onLogChangedListener){
        this.logChangedListeners.remove(onLogChangedListener);
    }

    public void log(final String tag, final String message){
        Log.d(tag, message);
        for (final OnLogChangedListener onLogChangedListener:this.logChangedListeners) {
            onLogChangedListener.onLogChanged(tag, message);
        }
    }
}
