package ch.papers.androidcommunicationbenchmark.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import ch.papers.androidcommunicationbenchmark.R;

/**
 * Created by Alessandro De Carli (@a_d_c_) on 06/12/15.
 * Papers.ch
 * a.decarli@papers.ch
 */
public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    public static Fragment newInstance() {
        return new PreferencesFragment();
    }
}
