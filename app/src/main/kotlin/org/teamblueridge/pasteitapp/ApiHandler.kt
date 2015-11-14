package org.teamblueridge.pasteitapp

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.util.JsonReader
import android.util.Log
import org.teamblueridge.utils.NetworkUtil
import java.io.FileInputStream
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

    /**
     * Gets a list of the languages supported by the remote server
     *
     * @param context     Used in order to be able to read the JSON file
     * @param mPrettyUgly Used in order to identify which Array<String> to use
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
     * @param sharedPrefs The shared preferences to be used for determining the domains, etc.
     */
    fun getLanguagesAvailable(sharedPrefs: SharedPreferences, context: Context) {
        GetLanguages(context as Activity).execute(UploadDownloadUrlPrep().prepUrl(sharedPrefs,
                                                                                  "downLangs"),
                                                  "languages", context)
    }

    /**
     * ASyncTask to get the languages from the server and write them to a file, "languages" which
     * can be read later
     */
    inner class GetLanguages(private val activity: Activity) : AsyncTask<Any, String, Boolean>() {
        /**
         * Read the languages from the server to a file
         *
         * @param params Two Strings: languageUrl and filename; One Context: context
         * <p>languageUrl: URL the languages can be fetched from
         * <br />filename: The name of the file to be written to
         * <br />context: The current context used for writing the file
         * @return True if the downloaded JSON is valid, false otherwise
         */
        override fun doInBackground(vararg params: Any): Boolean {
            val languageUrl = params[0] as String
            val filename = params[1] as String
            val context = params[2] as Context
            val languageList = NetworkUtil.readFromUrl(languageUrl)
            context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                if (languageList.length > 0) {
                    it.write(languageList.toByteArray())
                    return true
                } else {
                    it.write("{\"text\":\"Plain Text\"}".toByteArray())
                    return false
                }
            }
        }

        /**
         * Display an alert dialog if there is a problem with the JSON file
         *
         * @param valid True if the downloaded JSON is valid, false otherwise
         */
        public override fun onPostExecute(valid: Boolean) {
            if (!valid) {
                activity.runOnUiThread {
                    val builder1 = AlertDialog.Builder(activity)
                    builder1.setMessage(activity.resources.getString(R.string.error_api_key))
                    builder1.setCancelable(true)
                    builder1.setNeutralButton(android.R.string.ok,
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface, id: Int) {
                                    dialog.cancel()
                                }
                            })
                    val alert11 = builder1.create()
                    alert11.show()
                }
            }
        }
    }

    companion object {
        private val TAG = "TeamBlueRidge"
    }
}
