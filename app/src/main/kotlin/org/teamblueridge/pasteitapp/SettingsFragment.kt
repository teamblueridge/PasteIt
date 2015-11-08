package org.teamblueridge.pasteitapp

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager

class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    //private static final String TAG = "TeamBlueRidge";

    /* We have a listener that checks for any preference changes, so that we can act on it.
     * There is a case statement that actually acts on the change using Java 7's ability to use a
     * case statement with a string. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences)
        updateApiPreferenceSummary("pref_api_key")
        val listPreference = findPreference("pref_default_language") as ListPreference
        setListPreferenceData(listPreference)
        findPreference("pref_default_language").summary = listPreference.entry
        findPreference("pref_version").summary = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        //Make sure the listener actually gets registered
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        //Unregister the listener once settings is no longer active
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        //Run a check to see which preference was changed and act on it
        val apiHandler = ApiHandler()
        when (key) {
            "pref_api_key" -> //Update the summary to show the new value
                updateApiPreferenceSummary(key)
            "pref_domain" -> {
                //Reset the API key, because it may vary by domain and then reload the preferences
                findPreference("pref_api_key").editor.putString("pref_api_key", "").commit()
                preferenceScreen = null
                addPreferencesFromResource(R.xml.preferences)
                updateApiPreferenceSummary("pref_api_key")
                apiHandler.getLanguagesAvailable(sharedPreferences, activity)
            }
            "pref_name" -> {
            }
            "pref_default_language" -> {
                val listPreference = findPreference(key) as ListPreference
                findPreference(key).summary = listPreference.entry
            }
            else -> {
            }
        }//We don't do anything special for changing the name, but it's nice to have it here
    }

    protected fun setListPreferenceData(listPreference: ListPreference) {
        val apiHandler = ApiHandler()
        listPreference.entries = apiHandler.getLanguageArray(activity, "pretty")
        listPreference.setDefaultValue("1")
        listPreference.entryValues = apiHandler.getLanguageArray(activity, "ugly")
    }

    /**
     * Updates the summary for the API key preference. Called from both the "pref_api_key" and
     * "pref_domain" cases in onSharedPreferenceChanged();

     * @param key : The API key that the summary should be updated to
     */
    fun updateApiPreferenceSummary(key: String) {
        //Update the API key summary, called in 3 places
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        if (!prefs.getString(key, "")!!.isEmpty()) {
            findPreference(key).summary = getString(R.string.pref_api_key_summary,
                    prefs.getString(key, ""))
        } else {
            findPreference(key).summary = getString(R.string.pref_api_key_summary_ifempty)
        }

    }

}