package org.teamblueridge.pasteitapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    /* We have a listener that checks for any preference changes, so that we can act on it.
     * There is a case statement that actually acts on the change using Java 7's ability to use a
     * case statement with a string. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        updateApiPreferenceSummary("pref_api_key");
        ListPreference listPreference = (ListPreference) findPreference("pref_default_language");
        setListPreferenceData(listPreference);
        findPreference("pref_default_language").setSummary(listPreference.getEntry());
        findPreference("pref_version").setSummary(BuildConfig.VERSION_NAME);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Make sure the listener actually gets registered
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Unregister the listener once settings is no longer active
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Run a check to see which preference was changed and act on it
        ApiHandler apiHandler = new ApiHandler();
        switch (key) {
            case "pref_api_key":
                //Update the summary to show the new value
                updateApiPreferenceSummary(key);
                break;
            case "pref_domain":
                //Reset the API key, because it may vary by domain and then reload the preferences
                findPreference("pref_api_key").getEditor().putString("pref_api_key", "").commit();
                setPreferenceScreen(null);
                addPreferencesFromResource(R.xml.preferences);
                updateApiPreferenceSummary("pref_api_key");
                apiHandler.getLanguagesAvailable(sharedPreferences, getActivity());
                break;
            case "pref_name":
                //We don't do anything special for changing the name, but it's nice to have it here
                break;
            case "pref_default_language":
                ListPreference listPreference = (ListPreference) findPreference(key);
                findPreference(key).setSummary(listPreference.getEntry());
                break;
            default:
                break;
        }
    }

    protected void setListPreferenceData(ListPreference listPreference) {
        ApiHandler apiHandler = new ApiHandler();
        listPreference.setEntries(apiHandler.getLanguageArray(getActivity(), "pretty"));
        listPreference.setDefaultValue("1");
        listPreference.setEntryValues(apiHandler.getLanguageArray(getActivity(), "ugly"));
    }

    /**
     * Updates the summary for the API key preference. Called from both the "pref_api_key" and
     * "pref_domain" cases in onSharedPreferenceChanged();
     *
     * @param key : The API key that the summary should be updated to
     */
    public void updateApiPreferenceSummary(String key) {
        //Update the API key summary, called in 3 places
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (!prefs.getString(key, "").isEmpty()) {
            findPreference(key).setSummary(getString(R.string.pref_api_key_summary,
                    prefs.getString(key, "")));
        } else {
            findPreference(key).setSummary(getString(R.string.pref_api_key_summary_ifempty));
        }

    }

}