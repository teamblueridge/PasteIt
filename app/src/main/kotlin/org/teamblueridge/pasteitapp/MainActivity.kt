package org.teamblueridge.pasteitapp

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.activity_main.*
import kotlinx.android.synthetic.fragment_paste.*
import org.teamblueridge.utils.NetworkUtil
import java.io.*
import java.net.URL

import com.pawegio.kandroid.runAsync
import com.pawegio.kandroid.runOnUiThread
import com.pawegio.kandroid.toast
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {

    private var mReceivedIntent: Intent? = null
    private var mReceivedAction: String? = null
    private val TAG = "TeamBlueRidge"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        // Set-up the paste fragment and give it a name so we can track it
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .add(R.id.container, PasteFragment(), "PasteFragment")
                .commit()
        }

        // Set-up up navigation
        fragmentManager.addOnBackStackChangedListener {
            /* Check if we have anything on the stack. If we do, we need to have the back button
             * display in in the ActionBar */
            if (fragmentManager.backStackEntryCount > 0) {
                supportActionBar.setHomeButtonEnabled(true)
                supportActionBar.setDisplayHomeAsUpEnabled(true)
            } else {
                supportActionBar.setHomeButtonEnabled(false)
                supportActionBar.setDisplayHomeAsUpEnabled(true)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.blue_700)
        }
    }

    public override fun onStart() {
        super.onStart()
        mReceivedIntent = intent
        mReceivedAction = mReceivedIntent!!.action
        if (mReceivedAction == Intent.ACTION_VIEW || mReceivedAction == Intent.ACTION_EDIT) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat
                    .requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                loadFile()
            } else {
                toast(getString(R.string.request_permissions))
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                loadFile()
            }
            mReceivedAction = Intent.ACTION_DEFAULT
        } else {
            paste_content_edittext.setText(mReceivedIntent!!.getStringExtra(Intent.EXTRA_TEXT))
        }

        // Check if the "languages" file exists
        if (File("languages").exists()) {
            populateSpinner()
        } else {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
            ApiHandler().getLanguages(this as Activity, UploadDownloadUrlPrep().prepUrl(prefs,
                                      UploadDownloadUrlPrep.DOWNLOAD_LANGS));
            populateSpinner()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_paste -> {
                prepForPaste()
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            android.R.id.home -> {
                fragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Does some preparation before finally calling the UploadPaste ASyncTask
     */
    fun prepForPaste() {
        val languageListStringArray = ApiHandler().getLanguageArray(applicationContext,
                                                                    ApiHandler.UGLY_LIST)
        val language = languageListStringArray[language_spinner.selectedItemPosition]
        if (!paste_content_edittext.text.toString().isEmpty()) {
            if (NetworkUtil.isConnectedToNetwork(this)) {
                if (language != "0") {
                    doPaste(if (!language.isEmpty()) language else "text")
                    toast(getString(R.string.paste_toast))
                    paste_name_edittext.setText("")
                    paste_content_edittext.setText("")
                } else {
                    toast(getString(R.string.invalid_language))
                }
            } else {
                toast(getString(R.string.no_network))
            }
        } else {
            toast(getString(R.string.paste_no_text))
        }
    }

    /**
     * Uploads the paste to the server then receives the URL of the paste and displays it in the
     * Paste URL Label.
     *
     * @param receivedLanguage  Language to upload the paste in
     */
    fun doPaste(receivedLanguage: String) {
        val httpUserAgent = ("Paste It v${getString(R.string.version_name)}" +
            ", an Android app for pasting to Stikked " +
            "(https://play.google.com/store/apps/details?id=org.teamblueridge.pasteitapp)")
        val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val pDialogUpload = ProgressDialog(this@MainActivity)
        val language = if (!receivedLanguage.isEmpty()) receivedLanguage else "text"
        val title = paste_name_edittext.text.toString()
        val text = paste_content_edittext.text.toString()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        //Ensure username is set, if not, default to "mobile user"
        val mUserName =
            if (!prefs.getString("pref_name", "").isEmpty())
                prefs.getString("pref_name", "")
            else
                "Mobile User " + getString(R.string.version_name)

        val url = URL(UploadDownloadUrlPrep().prepUrl(prefs, UploadDownloadUrlPrep.UPLOAD_PASTE))
        val urlConnection = url.openConnection() as HttpsURLConnection

        pDialogUpload.setMessage(getString(R.string.paste_upload))
        pDialogUpload.isIndeterminate = false
        pDialogUpload.setCancelable(false)
        pDialogUpload.show()
        runAsync {
            // HTTP Header data
            urlConnection.requestMethod = "POST"
            urlConnection.doInput = true
            urlConnection.doOutput = true
            urlConnection.setRequestProperty("User-Agent", httpUserAgent)
            val builder = Uri.Builder()
                    .appendQueryParameter("title", title)
                    .appendQueryParameter("text", text)
                    .appendQueryParameter("name", mUserName)
                    .appendQueryParameter("lang", language)
            val query = builder.build().encodedQuery
            val os = urlConnection.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
            writer.write(query)
            writer.flush()
            writer.close()
            os.close()
            urlConnection.connect()

            //Get the URL of the paste
            val urlStringBuilder = StringBuilder()
            BufferedReader(InputStreamReader(urlConnection.inputStream))
                    .forEachLine { urlStringBuilder.append(it) }

            val pasteUrl = urlStringBuilder.toString()
            pDialogUpload.dismiss()
            if (Patterns.WEB_URL.matcher(pasteUrl).matches())
                clipboard.primaryClip = ClipData.newPlainText("PasteIt", pasteUrl)

            runOnUiThread {
                if (Patterns.WEB_URL.matcher(pasteUrl).matches()) {
                    paste_url_label.text = Html.fromHtml("<a href=\"$pasteUrl\">$pasteUrl</a>")
                    paste_url_label.movementMethod = LinkMovementMethod.getInstance()
                } else {
                    Log.e(TAG, "Bad URL: URL received was $pasteUrl")
                    paste_url_label.text = getString(R.string.invalid_url)
                }
            }
        }
    }

    /**
     * Loads the file being opened and puts it into the paste content EditText.
     */
    fun loadFile() {
        val pDialogFileLoad = ProgressDialog(this@MainActivity)
        pDialogFileLoad.setMessage(getString(R.string.file_load))
        pDialogFileLoad.isIndeterminate = true
        pDialogFileLoad.setCancelable(false)
        pDialogFileLoad.show()
        runAsync {
            mReceivedAction = intent.action
            val receivedText = mReceivedIntent!!.data
            val inputStream = contentResolver.openInputStream(receivedText)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            reader.forEachLine { stringBuilder.append(it + "\n") }
            inputStream.close()
            runOnUiThread { paste_content_edittext.setText(stringBuilder.toString()) }
        }
        pDialogFileLoad.dismiss()
    }

    /**
     * Populates the spinner for language selection using the arrays of languages generated by
     * the API handler.
     */
    fun populateSpinner() {
        val langListPretty = ApiHandler().getLanguageArray(applicationContext,
                                                           ApiHandler.PRETTY_LIST)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this@MainActivity)
        val positionListPref = prefs.getString("pref_default_language", "-1")
        val uglyList = ArrayAdapter(applicationContext, R.layout.spinner_item,
                                    ApiHandler().getLanguageArray(applicationContext,
                                                                  ApiHandler.UGLY_LIST))
        val adapter = ArrayAdapter(applicationContext, R.layout.spinner_item, langListPretty)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        language_spinner.adapter = adapter
        language_spinner.setSelection(uglyList.getPosition(positionListPref))
    }

}