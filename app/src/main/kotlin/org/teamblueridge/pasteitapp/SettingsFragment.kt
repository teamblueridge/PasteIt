package org.teamblueridge.pasteitapp

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.ListPreference
import android.preference.PreferenceFragment
import android.preference.PreferenceManager

/**
 * Provides the listeners to ensure that changes to preferences are handled properly.
 */
class SettingsFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        updateApiPreferenceSummary("pref_api_key")
        val listPreference = findPreference("pref_default_language") as ListPreference
        setListPreferenceData(listPreference)
        findPreference("pref_default_language").summary = listPreference.entry
        findPreference("pref_version").summary = BuildConfig.VERSION_NAME
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "pref_api_key" -> updateApiPreferenceSummary(key)
            "pref_domain" -> {
                findPreference("pref_api_key").editor.putString("pref_api_key", "").commit()
                preferenceScreen = null
                addPreferencesFromResource(R.xml.preferences)
                updateApiPreferenceSummary("pref_api_key")
                ApiHandler().getLanguages(activity,
                                          UploadDownloadUrlPrep().prepUrl(sharedPreferences,
                                          UploadDownloadUrlPrep.DOWNLOAD_LANGS));
            }
            "pref_default_language" -> {
                findPreference(key).summary = (findPreference(key) as ListPreference).entry
            }
            else -> {}
        }
    }

    /**
     * Updates the languages available to be selected.
     *
     * @param listPreference  The preference to be updated
     */
    protected fun setListPreferenceData(listPreference: ListPreference) {
        listPreference.entries = ApiHandler().getLanguages(activity).keys.toTypedArray()
        listPreference.setDefaultValue("1")
        listPreference.entryValues = ApiHandler().getLanguages(activity).values.toTypedArray()
    }

    /**
     * Updates the summary for the API key preference. Called from both the "pref_api_key" and
     * "pref_domain" cases in [onSharedPreferenceChanged].
     *
     * @param key  The API key that the summary should be updated to
     */
    fun updateApiPreferenceSummary(key: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        findPreference(key).summary =
            if (!prefs.getString(key, "").isEmpty())
                getString(R.string.pref_api_key_summary, prefs.getString(key, ""))
            else
                getString(R.string.pref_api_key_summary_ifempty)
    }

}