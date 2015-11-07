package org.teamblueridge.pasteitapp

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.util.JsonReader
import android.util.Log

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList

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
        try {
            val fis: FileInputStream
            val langListUgly = ArrayList<String>()
            val langListPretty = ArrayList<String>()
            fis = context.openFileInput("languages")
            val reader = JsonReader(InputStreamReader(fis, "UTF-8"))
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
            when (mPrettyUgly) {
                "pretty" -> return languageListPrettyStringArray
                "ugly" -> return languageListUglyStringArray
                else -> {
                    Log.e(TAG, "Unexpected array description")
                    return null
                }
            }

        } catch (e: IOException) {
            Log.e(TAG, e.toString())
            return null
        }

    }

    /**
     * Does some preparation before calling the GetLanguages ASyncTask.
     *
     * @param sharedPreferences The shared preferences to be used for determining the domains, etc.
     */
    fun getLanguagesAvailable(sharedPreferences: SharedPreferences, context: Context) {

        val languageUrl: String
        val filename = "languages"

        val upDownPrep = UploadDownloadUrlPrep()
        languageUrl = upDownPrep.prepUrl(sharedPreferences, "downLangs")
        val getLanguages = GetLanguages(context as Activity)
        getLanguages.execute(languageUrl, filename, context)

    }

    /**
     * ASyncTask to get the languages from the server and write them to a file, "languages" which
     * can be read later
     */
    inner class GetLanguages(private val activity: Activity) : AsyncTask<Any, String, Boolean>() {

        override fun doInBackground(vararg params: Any): Boolean {
            var languageUrl = params[0] as String
            var filename = params[1] as String
            var context = params[2] as Context
            var languageList = NetworkUtil.readFromUrl(languageUrl)
            try {
                context.openFileOutput(filename, Context.MODE_PRIVATE).use {
                    if (languageList.length > 0) {
                        it.write(languageList.toByteArray())
                        return true
                    } else {
                        it.write("{\"text\":\"Plain Text\"}".toByteArray())
                        return false
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
                e.printStackTrace()
                return false
            }

        }

        public override fun onPostExecute(valid: Boolean) {
            if (!valid) {
                activity.runOnUiThread(object : Runnable {
                    override fun run() {

                        val builder1 = AlertDialog.Builder(activity)
                        builder1.setMessage("Your API key is incorrect. Please fix it in settings."
                                + " The only language available for uploads is Plain Text")
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
                })
            }
        }
    }

    companion object {

        private val TAG = "TeamBlueRidge"
    }
}
