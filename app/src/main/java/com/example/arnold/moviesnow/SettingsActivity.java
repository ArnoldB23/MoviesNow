package com.example.arnold.moviesnow;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;


/**
 * Created by Arnold on 10/25/2015.
 */
public class SettingsActivity extends PreferenceActivity {
    public static final String LOG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        final Map<String,?> allPrefKeys = sharedPref.getAll();

        for(final String k : allPrefKeys.keySet())
        {
            Log.d(LOG_TAG, "Key " + k);

            final CheckBoxPreference checkBoxPref = (CheckBoxPreference)findPreference(k);
            checkBoxPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    for(String otherkey : allPrefKeys.keySet())
                    {
                        if ( !otherkey.equals(k))
                        {
                            CheckBoxPreference otherCheckBoxPref = (CheckBoxPreference)findPreference(otherkey);
                            otherCheckBoxPref.setChecked(false);
                        }
                    }

                    checkBoxPref.setChecked(true);

                    finish();
                    return true;
                }
            });
        }

    }

}
