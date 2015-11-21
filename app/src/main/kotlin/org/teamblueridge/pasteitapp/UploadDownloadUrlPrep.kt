package org.teamblueridge.pasteitapp

import android.content.SharedPreferences
import android.util.Log

/**
 * Handles the URLs for uploading pastes, downloading languages, and downloading
 * recent pastes.
 *
 * @author Kyle Laker (kalaker)
 * @version 1.0
 */
class UploadDownloadUrlPrep {

    companion object {
        public val UPLOAD_PASTE = 1
        public val DOWNLOAD_LANGS = 2
        public val DOWNLOAD_RECENT = 3
        private val TAG = "TeamBlueRidge"
    }
    /**
     * Gets the proper URL for uploading a paste or for downloading a list of recent pastes (JSON)
     * or a list of languages the server supports (JSON) for syntax highlighting
     *
     * @param prefs   The preferences to be able to access the domain, API key, etc.
     * @param upDown  One of three options, depending on the API information needed
     *                <li><code>UPLOAD_PASTE</code>: creating a new paste
     *                <li><code>DOWNLOAD_LANGS</code>: getting languages
     *                <li><code>DOWNLOAD_RECENT</code>: recent pasts
     * @return The URL to be used, with the API key if necessary
     */
    fun prepUrl(prefs: SharedPreferences, upDown: Int): String {
        val TEAMBLUERIDGE_APIKEY = "teamblueridgepaste"
        val LANGS_WITH_APIKEY = "/api/langs?apikey="
        val CREATE_WITH_APIKEY = "/api/create?apikey="
        val RECENT_WITH_APIKEY = "/api/recent?apikey="

        val mLangDownloadUrl: String
        val mRecentDownloadUrl: String
        val mUploadUrl: String

        val mPasteDomain =
            if (!prefs.getString("pref_domain", "").isEmpty())
                prefs.getString("pref_domain", "")
            else
                "https://paste.teamblueridge.org"

        // Only set the API key for Team BlueRidge because we know our key
        if (mPasteDomain.equals("https://paste.teamblueridge.org")) {
            mUploadUrl = mPasteDomain + CREATE_WITH_APIKEY + TEAMBLUERIDGE_APIKEY
            mLangDownloadUrl = mPasteDomain + LANGS_WITH_APIKEY + TEAMBLUERIDGE_APIKEY
            mRecentDownloadUrl = mPasteDomain + RECENT_WITH_APIKEY + TEAMBLUERIDGE_APIKEY
        } else if (!prefs.getString("pref_api_key", "").isEmpty()) {
            val mPasteApiKey = prefs.getString("pref_api_key", "")
            mUploadUrl = mPasteDomain + CREATE_WITH_APIKEY + mPasteApiKey
            mLangDownloadUrl = mPasteDomain + LANGS_WITH_APIKEY + mPasteApiKey
            mRecentDownloadUrl = mPasteDomain + RECENT_WITH_APIKEY + mPasteApiKey
        } else {
            mUploadUrl = mPasteDomain + "/api/create"
            mLangDownloadUrl = mPasteDomain + "/api/langs"
            mRecentDownloadUrl = mPasteDomain + "/api/recent"
        }
        return when (upDown) {
            UPLOAD_PASTE    -> mUploadUrl
            DOWNLOAD_LANGS  -> mLangDownloadUrl
            DOWNLOAD_RECENT -> mRecentDownloadUrl
            else -> {
                Log.e(TAG, "Unknown URL case")
                ""
            }
        }
    }
}