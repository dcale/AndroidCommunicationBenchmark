package ch.papers.androidcommunicationbenchmark.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class Preferences {


    private static Preferences INSTANCE;

    public static Preferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Preferences();
        }
        return INSTANCE;
    }

    private Preferences() {
    }

    private Context context;

    public void init(Context context){
        this.context = context;
    }

    public int getCycleCount(){
        return this.getInt("pref_cycle_count", Constants.CYCLE_COUNT);
    }

    public int getPayloadSize(){
        return this.getInt("pref_payload_size", Constants.DEFAULT_PAYLOAD_SIZE);
    }

    private int getInt(String preferenceKey, int defaultInt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
        try{
            return Integer.parseInt(prefs.getString(preferenceKey, ""+defaultInt));
        } catch (Exception e){
            return defaultInt;
        }
    }
}
