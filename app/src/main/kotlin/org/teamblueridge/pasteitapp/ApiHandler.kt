package org.teamblueridge.pasteitapp

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AlertDialog
import android.util.JsonReader
import android.util.Log
import com.pawegio.kandroid.runAsync;
import org.teamblueridge.utils.NetworkUtil
import java.io.InputStreamReader
import java.util.*

/**
 * Communicates with the Stikked API to get the data necessary
 *
 * @author Kyle Laker (kalaker)
 * @see UploadDownloadUrlPrep
 */
class ApiHandler {

    /**
     * Gets a list of the languages supported by the remote server
     *
     * @param context  Used in order to be able to read the JSON file
     * @param mPrettyUgly  Used in order to identify which Array<String> to use
     * @return a Array<String>, either pretty or ugly, depending on what is needed
     */
    fun getLanguageArray(context: Context, mPrettyUgly: String): Array<String>? {
        val langListUgly = ArrayList<String>()
        val langListPretty = ArrayList<String>()
        val reader = JsonReader(InputStreamReader(context.openFileInput("languages"), "UTF-8"))
        reader.beginObject()
        while (reader.hasNext()) {
            langListUgly.add(reader.nextName())
            langListPretty.add(reader.nextString())
        }
        reader.endObject()
        val languageListUglyStringArray =
                langListUgly.toArray<String>(arrayOfNulls<String>(langListUgly.size))
        val languageListPrettyStringArray =
                langListPretty.toArray<String>(arrayOfNulls<String>(langListPretty.size))
        reader.close()

        return when (mPrettyUgly) {
            "pretty" -> languageListPrettyStringArray
            "ugly" -> languageListUglyStringArray
            else -> {
                Log.e(TAG, "Unexpected array description")
                null
            }
        }
    }

    /**
     * Does some preparation before calling the GetLanguages ASyncTask.
     *
     * @param sharedPrefs  The shared preferences to be used for determining the domains, etc.
     * @param context  The context of the calling activity
     */
    fun getLanguagesAvailable(sharedPrefs: SharedPreferences, context: Context) {
        getLanguages(context, UploadDownloadUrlPrep().prepUrl(sharedPrefs, "downLangs"));
    }

    /**
     * Downloads the list of languages from the Stikked API.
     *
     * @param context  The context of the calling activity
     * @param languageUrl  The URL from which to fetch the languages
     * @param filename  The file to which to write the languages. Defaults to "languages".
     */
    fun getLanguages(context: Context, languageUrl: String, filename: String = "languages")
    {
        val activity = context as Activity

        runAsync {
            val languageList = NetworkUtil.readFromUrl(languageUrl)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                if (languageList.length > 0) {
                    it.write(languageList.toByteArray())
                } else {
                    it.write("{\"text\":\"Plain Text\"}".toByteArray())
                    activity.runOnUiThread {
                        val builder1 = AlertDialog.Builder(activity)
                        builder1.setMessage(activity.resources.getString(R.string.error_api_key))
                        builder1.setCancelable(true)
                        builder1.setNeutralButton(android.R.string.ok,
                                { dialog, id -> dialog.cancel() })
                        val alert11 = builder1.create()
                        alert11.show()
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = "TeamBlueRidge"
    }
}
