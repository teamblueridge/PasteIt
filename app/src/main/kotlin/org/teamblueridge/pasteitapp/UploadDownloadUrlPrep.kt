package org.teamblueridge.pasteitapp

import android.content.SharedPreferences
import android.util.Log

/**
 * Handle the URLs for uploading pastes, downloading languages, and downloading
 * recent pastes
 *
 * @author Kyle Laker (kalaker)
 * @version 1.0
 */
class UploadDownloadUrlPrep {

    /**
     * Gets the proper URL for uploading a paste or for downloading a list of recent pastes (JSON)
     * or a list of languages the server supports (JSON) for syntax highlighting
     *
     * @param prefs  The preferences to be able to access the domain, API key, etc.
     * @param upDown "upCreate" for creating a new paste<p>
     *               "downLangs" for getting languages<p>
     *               "downRecents" for recent pasts
     * @return The URL to be used, with the API key if necessary
     */
    fun prepUrl(prefs: SharedPreferences, upDown: String): String {
        val TAG = "TeamBlueRidge"
        val TEAMBLUERIDGE_APIKEY = "teamblueridgepaste"
        val LANGS_WITH_APIKEY = "/api/langs?apikey="
        val CREATE_WITH_APIKEY = "/api/create?apikey="
        val RECENT_WITH_APIKEY = "/api/recent?apikey="

        // Ensure that the paste URL is set, if not, default to Team BlueRidge
        var mPasteDomain: String
        var mLangDownloadUrl: String
        var mRecentDownloadUrl: String
        var mUploadUrl: String

        if (prefs.getString("pref_domain", "").length != 0)
            mPasteDomain = prefs.getString("pref_domain", "")
        else
            mPasteDomain = "https://paste.teamblueridge.org"

        // Only set the API key for Team BlueRidge because we know our key
        if (mPasteDomain == "https://paste.teamblueridge.org") {
            mUploadUrl = mPasteDomain + CREATE_WITH_APIKEY + TEAMBLUERIDGE_APIKEY
            mLangDownloadUrl = mPasteDomain + LANGS_WITH_APIKEY + TEAMBLUERIDGE_APIKEY
            mRecentDownloadUrl = mPasteDomain + RECENT_WITH_APIKEY + TEAMBLUERIDGE_APIKEY
        } else {
            if (prefs.getString("pref_api_key", "").length != 0) {
                var mPasteApiKey = prefs.getString("pref_api_key", "")
                mUploadUrl = mPasteDomain + CREATE_WITH_APIKEY + mPasteApiKey
                mLangDownloadUrl = mPasteDomain + LANGS_WITH_APIKEY + mPasteApiKey
                mRecentDownloadUrl = mPasteDomain + RECENT_WITH_APIKEY + mPasteApiKey
            } else {
                mUploadUrl = mPasteDomain + "/api/create"
                mLangDownloadUrl = mPasteDomain + "/api/langs"
                mRecentDownloadUrl = mPasteDomain + "/api/recent"
            }
        }

        when (upDown) {
            "upCreate" -> return mUploadUrl
            "downLangs" -> return mLangDownloadUrl
            "downRecent" -> return mRecentDownloadUrl
            else -> {
                Log.e(TAG, "Unknown URL case")
                return ""
            }
        }
    }
}