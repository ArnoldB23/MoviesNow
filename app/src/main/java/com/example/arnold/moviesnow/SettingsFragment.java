package com.example.arnold.moviesnow;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Arnold on 10/25/2015.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
