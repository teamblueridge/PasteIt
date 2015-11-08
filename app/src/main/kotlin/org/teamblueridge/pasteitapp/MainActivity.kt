package org.teamblueridge.pasteitapp

import android.Manifest
import android.app.FragmentManager
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var mPasteUrlString: String? = null
    private var mFileContents: String? = null
    private var mReceivedIntent: Intent? = null
    private var mReceivedAction: String? = null
    private var pDialogUpload: ProgressDialog? = null
    private val TAG = "TeamBlueRidge"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        //We're using the toolbar from AppCompat as our actionbar
        val toolbar = findViewById(R.id.toolbar) as Toolbar

        setSupportActionBar(toolbar)
        // Set-up the paste fragment and give it a name so we can track it
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction().add(R.id.container, PasteFragment(), "PasteFragment").commit()
        }

        // Set-up up navigation
        fragmentManager.addOnBackStackChangedListener(object : FragmentManager.OnBackStackChangedListener {
            override fun onBackStackChanged() {
                val stackHeight = fragmentManager.backStackEntryCount
                /* Check if we have anything on the stack. If we do, we need to have the back button
                 * display in in the ActionBar */
                if (supportActionBar != null) {
                    if (stackHeight > 0) {
                        supportActionBar!!.setHomeButtonEnabled(true)
                        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                    } else {
                        supportActionBar!!.setDisplayHomeAsUpEnabled(false)
                        supportActionBar!!.setHomeButtonEnabled(false)
                    }
                }
            }

        })

        // Set up the status bar tint using the CarbonROM SysBarTintManager
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) and (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)) {
            SysBarTintManager.setupTranslucency(this, true, false)
            val mTintManager = SysBarTintManager(this)
            mTintManager.isStatusBarTintEnabled = true
            mTintManager.isActionBarTintEnabled = true
            mTintManager.setStatusBarTintColor(ContextCompat.getColor(this, R.color.blue_700))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.blue_700)
        }

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }*/

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage(R.string.permission_explanation)
                        .setTitle(R.string.permission_explanation_title);
                var dialog: AlertDialog = builder.create()
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        0);
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                0);
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        mReceivedIntent = intent
        mReceivedAction = mReceivedIntent!!.action
        val pasteContentEditText = findViewById(R.id.paste_content_edittext) as EditText
        if (mReceivedAction == Intent.ACTION_VIEW || mReceivedAction == Intent.ACTION_EDIT) {
            val receivingText = resources.getString(R.string.file_load)
            //Prepare the dialog for loading a file
            val pDialogFileLoad = ProgressDialog(this@MainActivity)
            pDialogFileLoad.setMessage(receivingText)
            pDialogFileLoad.isIndeterminate = true
            pDialogFileLoad.setCancelable(false)
            pDialogFileLoad.show()
            LoadFile().execute()
            if (pDialogFileLoad.isShowing) {
                pDialogFileLoad.dismiss()
            }
            mReceivedAction = Intent.ACTION_DEFAULT
        } else {
            val receivedText = mReceivedIntent!!.getStringExtra(Intent.EXTRA_TEXT)
            if (receivedText != null) {
                pasteContentEditText.setText(receivedText)
            }
        }

        // Check if the "languages" file exists
        val file = baseContext.getFileStreamPath("languages")
        if (file.exists()) {
            PopulateSpinner().execute()
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            val apiHandler = ApiHandler()
            apiHandler.getLanguagesAvailable(prefs, this)
            PopulateSpinner().execute()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        when (id) {
            R.id.action_paste -> {
                doPaste()
                return true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            android.R.id.home -> {
                fragmentManager.popBackStack()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Does some preparation before finally calling the UploadPaste ASyncTask
     */
    fun doPaste() {
        val pasteNameEditText = findViewById(R.id.paste_name_edittext) as EditText
        val pasteContentEditText = findViewById(R.id.paste_content_edittext) as EditText
        val pasteContentString = pasteContentEditText.text.toString()
        val languageSpinner = findViewById(R.id.language_spinner) as Spinner
        val languageSelected = languageSpinner.selectedItemPosition
        val apiHandler = ApiHandler()
        val mToastText: String
        val languageListStringArray = apiHandler.getLanguageArray(applicationContext, "ugly")

        val language = languageListStringArray!![languageSelected]
        if (!pasteContentString.isEmpty()) {
            if (NetworkUtil.isConnectedToNetwork(this)) {
                if (language != "0") {
                    UploadPaste().execute(language)
                    mToastText = resources.getString(R.string.paste_toast)
                    pasteNameEditText.setText("")
                    pasteContentEditText.setText("")
                } else {
                    mToastText = resources.getString(R.string.invalid_language)
                }
            } else {
                mToastText = resources.getString(R.string.no_network)
            }
        } else {
            mToastText = resources.getString(R.string.paste_no_text)
        }
        val context = applicationContext
        val text = mToastText
        val duration = Toast.LENGTH_SHORT
        Toast.makeText(context, text, duration).show()
    }

    /**
     * Sets the title of the ActionBar to be whatever is specified. Called from the various
     * fragments and other activities

     * @param title The title to be used for the ActionBar
     */
    fun setActionBarTitle(title: String) {

        //Set the title of the action bar
        //Called from the fragments
        if (supportActionBar != null) {
            supportActionBar!!.title = title
        }
    }

    /**
     * AsyncTask for loading the file, from an intent, into the paste content EditText
     *
     *
     * doInBackground : get the file's contents loaded
     * onPostExecute : update the EditText's content
     */
    internal inner class LoadFile : AsyncTask<String, String, String>() {
        /**
         * Get the file's contents loaded

         * @param args unused
         * *
         * @return file contents
         */
        override fun doInBackground(vararg args: String): String? {
            try {
                mReceivedAction = intent.action
                val receivedText = mReceivedIntent!!.data
                val inputStream = contentResolver.openInputStream(receivedText)
                val reader = BufferedReader(InputStreamReader(
                        inputStream))
                val stringBuilder = StringBuilder()
                reader.forEachLine {stringBuilder.append(it + "\n")}
                inputStream!!.close()
                mFileContents = stringBuilder.toString()
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }

            return mFileContents
        }

        /**
         * Puts the contents of the file into the EditText

         * @param file contents of file
         */
        override fun onPostExecute(file: String?) {
            mFileContents = file
            runOnUiThread(object : Runnable {
                override fun run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    val pasteContentEditText: EditText
                    pasteContentEditText = findViewById(R.id.paste_content_edittext) as EditText
                    pasteContentEditText.setText(mFileContents)
                }
            })
        }
    }

    /**
     * ASyncTask that uploads the paste to the selected server
     *
     *
     * onPreExecute : Setup the upload dialog
     * doInBackground : Perform the upload operation with an HttpClient
     * onPostExecute : Close the dialog box and update the paste url label
     */
    internal inner class UploadPaste : AsyncTask<String, String, String>() {
        val HTTP_USER_AGENT = "Paste It v" + getString(R.string.version_name) + ", an Android app for pasting to Stikked " + "(https://play.google.com/store/apps/details?id=org.teamblueridge.pasteitapp)"
        var prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        var pasteUrlLabel = findViewById(R.id.paste_url_label) as TextView
        var pasteNameEditText = findViewById(R.id.paste_name_edittext) as EditText
        var pasteNameString = pasteNameEditText.text.toString()
        var pasteContentEditText = findViewById(R.id.paste_content_edittext) as EditText
        var pasteContentString = pasteContentEditText.text.toString()

        override fun onPreExecute() {
            val mUploadingText: String

            mUploadingText = resources.getString(R.string.paste_upload)
            super.onPreExecute()
            //Prepare the dialog for uploading the paste
            pDialogUpload = ProgressDialog(this@MainActivity)
            pDialogUpload!!.setMessage(mUploadingText)
            pDialogUpload!!.isIndeterminate = false
            pDialogUpload!!.setCancelable(false)
            pDialogUpload!!.show()
        }

        // post the content in the background while showing the dialog
        override fun doInBackground(vararg args: String): String? {
            val mUploadUrl: String
            val mUserName: String

            try {
                //HttpClient httpclient = new DefaultHttpClient();
                val upDownPrep = UploadDownloadUrlPrep()
                mUploadUrl = upDownPrep.prepUrl(prefs, "upCreate")
                var language: String? = args[0]

                //Ensure username is set, if not, default to "mobile user"
                if (!prefs.getString("pref_name", "")!!.isEmpty()) {
                    mUserName = prefs.getString("pref_name", "")
                } else {
                    mUserName = "Mobile User " + getString(R.string.version_name)
                }
                if (language == null || language.isEmpty()) {
                    language = "text"
                }
                //Get ready to actually send everything to the server
                val url = URL(mUploadUrl)
                val urlConnection = url.openConnection() as HttpURLConnection
                // HTTP Header data
                urlConnection.requestMethod = "POST"
                urlConnection.doInput = true
                urlConnection.doOutput = true
                urlConnection.setRequestProperty("User-Agent", HTTP_USER_AGENT)
                val builder = Uri.Builder().appendQueryParameter("title", pasteNameString)
                        .appendQueryParameter("text", pasteContentString)
                        .appendQueryParameter("name", mUserName)
                        .appendQueryParameter("lang", language)
                val query = builder.build().encodedQuery

                val os = urlConnection.outputStream
                val writer = BufferedWriter(
                        OutputStreamWriter(os, "UTF-8"))
                writer.write(query)
                writer.flush()
                writer.close()
                os.close()
                urlConnection.connect()

                //Get the URL of the paste
                val stringbuilder = StringBuilder()
                val urlReader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                urlReader.forEachLine {stringbuilder.append(it)}
                mPasteUrlString = stringbuilder.toString()
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }

            return mPasteUrlString
        }

        //Since we used a dialog, we need to disable it
        override fun onPostExecute(paste_url: String) {
            //Dismiss the dialog after getting all products
            pDialogUpload!!.dismiss()
            //Copy pasteUrl to clipboard
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("PasteIt", mPasteUrlString)
            clipboard.primaryClip = clip

            //Display paste URL if allowed in preferences
            runOnUiThread(object : Runnable {
                override fun run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    val linkText = "<a href=\"$mPasteUrlString\">$mPasteUrlString</a>"
                    pasteUrlLabel.text = Html.fromHtml(linkText)
                    pasteUrlLabel.movementMethod = LinkMovementMethod.getInstance()
                }
            })
        }
    }

    /**
     * Puts the languages from JSON grabbed via the Stikked API into the Spinner
     *
     *
     * doInBackground : Read from JSON file, turn JSON into an array of objects and an array of
     * keys.
     * onPostExecute : Populate the spinner with the Array from JSON
     */
    internal inner class PopulateSpinner : AsyncTask<String, String, Array<String>>() {
        var apiHandler = ApiHandler()

        override fun doInBackground(vararg params: String): Array<String>? {
            //Read the JSON file from internal storage
            return apiHandler.getLanguageArray(applicationContext, "pretty")
        }

        override fun onPostExecute(langListPretty: Array<String>) {
            //Populate spinner
            super.onPostExecute(langListPretty)
            runOnUiThread(object : Runnable {
                override fun run() {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
                    val positionListPref = prefs.getString("pref_default_language", "-1")
                    val uglyList = ArrayAdapter(applicationContext, R.layout.spinner_item,
                            apiHandler.getLanguageArray(applicationContext, "ugly"))
                    try {
                        val spinner = findViewById(R.id.language_spinner) as Spinner
                        val adapter = ArrayAdapter(applicationContext,
                                R.layout.spinner_item, langListPretty)
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        spinner.adapter = adapter
                        spinner.setSelection(uglyList.getPosition(positionListPref))
                    } catch (e: NullPointerException) {
                        Log.e(TAG, e.toString())
                    }

                }
            })

        }
    }
}