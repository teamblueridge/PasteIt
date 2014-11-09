package org.teamblueridge.pasteitapp;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Class to handle the create, recent, and languages URLs for the Stikked API</p>
 *
 * @author Kyle Laker (kalaker)
 */
public class UploadDownloadUrlPrep {

    static final String TEAMBLUERIDGE_APIKEY = "teamblueridgepaste";
    static final String LANGS_WITH_APIKEY = "/api/langs?apikey=";
    static final String CREATE_WITH_APIKEY = "/api/create?apikey=";
    static final String RECENT_WITH_APIKEY = "/api/recent?apikey=";
    String mUrl;
    String mPasteDomain;
    String mLangDownloadUrl;
    String mRecentDownloadUrl;
    String mUploadUrl;
    String mPasteApiKey;

    /**
     * Gets the proper URL for uploading a paste or for downloading a list of recent pastes (JSON)
     * or a list of languages the server supports (JSON) for syntax highlighting</p>
     *
     * @param prefs  The preferences to be able to access the domain, API key, etc.
     * @param upDown "upCreate" for creating a new paste
     *               "downLangs" for getting languages
     *               "downRecents" for recent pasts
     * @return The URL to be used, with the API key if necessary
     */
    public String prepUrl(SharedPreferences prefs, String upDown) {

        // Ensure that the paste URL is set, if not, default to Team BlueRidge
        if (!prefs.getString("pref_domain", "").isEmpty()) {
            mPasteDomain = prefs.getString("pref_domain", "");
        } else {
            mPasteDomain = "https://paste.teamblueridge.org";
        }
        // Only set the API key for Team BlueRidge because we know our key
        if (mPasteDomain.equals("https://paste.teamblueridge.org")) {
            mUploadUrl = mPasteDomain + CREATE_WITH_APIKEY + TEAMBLUERIDGE_APIKEY;
            mLangDownloadUrl = mPasteDomain + LANGS_WITH_APIKEY + TEAMBLUERIDGE_APIKEY;
            mRecentDownloadUrl = mPasteDomain + RECENT_WITH_APIKEY + TEAMBLUERIDGE_APIKEY;
        } else {
            if (!prefs.getString("pref_api_key", "").isEmpty()) {
                mPasteApiKey = prefs.getString("pref_api_key", "");
                mUploadUrl = mPasteDomain + CREATE_WITH_APIKEY + mPasteApiKey;
                mLangDownloadUrl = mPasteDomain + LANGS_WITH_APIKEY + mPasteApiKey;
                mRecentDownloadUrl = mPasteDomain + RECENT_WITH_APIKEY + mPasteApiKey;
            } else {
                mUploadUrl = mPasteDomain + "/api/create";
                mLangDownloadUrl = mPasteDomain + "/api/langs";
                mRecentDownloadUrl = mPasteDomain + "/api/recent";
            }
        }
        switch (upDown) {
            case "upCreate":
                mUrl = mUploadUrl;
                break;
            case "downLangs":
                mUrl = mLangDownloadUrl;
                break;
            case "downRecent":
                mUrl = mRecentDownloadUrl;
                break;
            default:
                Log.e("TeamBlueRidge", "Unknown URL case");
                break;
        }


        return mUrl;
    }
}