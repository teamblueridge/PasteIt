package org.teamblueridge.pasteitapp

import android.app.Activity
import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.JsonReader
import android.util.Log
import com.pawegio.kandroid.runAsync;
import org.teamblueridge.utils.NetworkUtil
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * Communicates with the Stikked API to get the data necessary
 *
 * @author Kyle Laker (kalaker)
 * @see UploadDownloadUrlPrep
 */
class ApiHandler {
    companion object {
        public val UGLY_LIST = 1
        public val PRETTY_LIST = 2
        private val TAG = "TeamBlueRidge"
    }

    /**
     * Gets a list of the languages supported by the remote server
     *
     * @param context  Used in order to be able to read the JSON file
     * @param listType  Used in order to identify which Array<String> to use
     * @return a Array<String>, either pretty or ugly, depending on what is needed
     */
    fun getLanguageArray(context: Context, listType: Int): Array<String> {
        val langListUgly = ArrayList<String>()
        val langListPretty = ArrayList<String>()
        try {
            val reader = JsonReader(InputStreamReader(context.openFileInput("languages"), "UTF-8"))

            reader.beginObject()
            while (reader.hasNext()) {
                langListUgly.add(reader.nextName())
                langListPretty.add(reader.nextString())
            }
            reader.endObject()
            reader.close()

            return when (listType) {
                PRETTY_LIST ->
                    langListPretty.toArray<String>(arrayOfNulls<String>(langListPretty.size))
                UGLY_LIST ->
                    langListUgly.toArray<String>(arrayOfNulls<String>(langListUgly.size))
                else -> {
                    Log.e(TAG, "Unexpected array description")
                    arrayOf("")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading languages file.")
            Log.e(TAG, e.toString())
            /* At least return an array to prevent an NPE */
            return when (listType) {
                PRETTY_LIST ->
                    arrayOf("Plain Text")
                UGLY_LIST ->
                    arrayOf("text")
                else -> {
                    Log.e(TAG, "Unexpected array description")
                    arrayOf("")
                }
            }
        }
    }

    /**
     * Downloads the list of languages from the Stikked API.
     *
     * @param activity  The context of the calling activity
     * @param languageUrl  The URL from which to fetch the languages
     * @param filename  The file to which to write the languages. Defaults to "languages".
     */
    fun getLanguages(activity: Activity, languageUrl: String, filename: String = "languages")
    {
        runAsync {
            val languageList = NetworkUtil.readFromUrl(languageUrl)
            activity.openFileOutput(filename, Context.MODE_PRIVATE).use {
                if (languageList.length > 0) {
                    it.write(languageList.toByteArray())
                } else {
                    it.write("{\"text\":\"Plain Text\"}".toByteArray())
                    activity.runOnUiThread {
                        val alertBuilder = AlertDialog.Builder(activity)
                        alertBuilder.setMessage(activity.resources.getString(R.string.error_api_key))
                        alertBuilder.setCancelable(true)
                        alertBuilder.setNeutralButton(android.R.string.ok,
                                                  { dialog, id -> dialog.cancel() })
                        val alert = alertBuilder.create()
                        alert.show()
                    }
                }
            }
        }
    }
}
