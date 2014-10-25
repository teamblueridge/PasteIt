package org.teamblueridge.pasteitapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        ((MainActivity) getActivity()).setActionBarTitle("Settings");
        updatePreferenceSummary("pref_api_key");
    }
    @Override
    public void onResume(){
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }
    @Override
    public void onPause(){
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        switch (key) {
            case "pref_api_key":
                updatePreferenceSummary(key);
                Log.d("TeamBlueRidge", "API key changed");
                return;
            case "pref_domain":
                Log.d("TeamBlueRidge", "Domain changed to:"+prefs.getString(key,""));
                findPreference("pref_api_key").getEditor().putString("pref_api_key","").commit();
                setPreferenceScreen(null);
                addPreferencesFromResource(R.xml.preferences);
                updatePreferenceSummary("pref_api_key");
                return;
            case "pref_name":
                Log.d("TeamBlueRidge", "Name changed");
                return;
            default:
                return;
        }
    }

    public void updatePreferenceSummary(String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(!prefs.getString(key,"").isEmpty()) {
            findPreference(key).setSummary(getString(R.string.pref_api_key_summary, prefs.getString(key, "")));
        } else {
            findPreference(key).setSummary(getString(R.string.pref_api_key_summary_ifempty));
        }

    }
}