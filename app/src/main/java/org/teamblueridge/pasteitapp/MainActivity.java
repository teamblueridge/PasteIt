package org.teamblueridge.pasteitapp;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "TeamBlueRidge";

    String mPasteUrlString;
    String mUploadUrl;
    String mUploadingText;
    String mUserName;
    String mToastText;
    String mFileContents;
    Intent mReceivedIntent;
    String mReceivedAction;
    ProgressDialog pDialogFileLoad;
    private ProgressDialog pDialogUpload;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //We're using the toolbar from AppCompat as our actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().getThemedContext();
        // Set-up the paste fragment and give it a name so we can track it
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PasteFragment(), "PasteFragment")
                    .commit();
        }

        // Set-up up navigation
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager
                .OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getFragmentManager().getBackStackEntryCount();
                /* Check if we have anything on the stack. If we do, we need to have the back button
                 * display in in the ActionBar */
                if (stackHeight > 0) {
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    getSupportActionBar().setHomeButtonEnabled(false);
                }
            }

        });

        // Get tracker.
        Tracker t = ((Analytics) this.getApplication()).getTracker(
                Analytics.TrackerName.APP_TRACKER);

        // Set up the status bar tint using the carbonrom SysBarTintManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                & Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            SysBarTintManager.setupTranslucency(this, true, false);

            SysBarTintManager mTintManager = new SysBarTintManager(this);
            mTintManager.setStatusBarTintEnabled(true);
            mTintManager.setActionBarTintEnabled(true);
            mTintManager.setStatusBarTintColor(getResources().getColor(R.color.blue_700));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.blue_700));
        }
    }

    public void onStart() {
        super.onStart();
        mReceivedIntent = getIntent();
        mReceivedAction = mReceivedIntent.getAction();
        EditText pasteContentEditText = (EditText) findViewById(R.id.paste_content_edittext);
        if (mReceivedAction.equals(Intent.ACTION_VIEW) || mReceivedAction.equals(Intent.ACTION_EDIT)) {
            String receivingText = getResources().getString(R.string.file_load);
            //Prepare the dialog for loading a file
            pDialogFileLoad = new ProgressDialog(MainActivity.this);
            pDialogFileLoad.setMessage(receivingText);
            pDialogFileLoad.setIndeterminate(true);
            pDialogFileLoad.setCancelable(false);
            pDialogFileLoad.show();
            new LoadFile().execute();
            if (pDialogFileLoad.isShowing()) {
                pDialogFileLoad.dismiss();
            }
            mReceivedAction = Intent.ACTION_DEFAULT;
        } else {
            String receivedText = mReceivedIntent.getStringExtra(Intent.EXTRA_TEXT);
            if (receivedText != null) {
                pasteContentEditText.setText(receivedText);
            }
        }

        // Check if the "languages" file exists
        File file = getBaseContext().getFileStreamPath("languages");
        if (file.exists()) {
            new PopulateSpinner().execute();
        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            ApiHandler apiHandler = new ApiHandler();
            apiHandler.getLanguagesAvailable(prefs, this);
            new PopulateSpinner().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_paste:
                doPaste();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Does some preparation before finally calling the UploadPaste ASyncTask
     */
    public void doPaste() {
        EditText pasteNameEditText = (EditText) findViewById(R.id.paste_name_edittext);
        EditText pasteContentEditText = (EditText) findViewById(R.id.paste_content_edittext);
        String pasteContentString = pasteContentEditText.getText().toString();
        Spinner languageSpinner = (Spinner) findViewById(R.id.language_spinner);
        Integer languageSelected = languageSpinner.getSelectedItemPosition();
        ApiHandler apiHandler = new ApiHandler();
        String[] languageListStringArray =
                apiHandler.getLanguageArray(getApplicationContext(), "pretty");

        String language = languageListStringArray[languageSelected];
        if (!pasteContentString.isEmpty()) {
            if (NetworkUtil.isConnectedToNetwork(this)) {
                if (!language.equals("0")) {
                    new UploadPaste().execute(language);
                    mToastText = getResources().getString(R.string.paste_toast);
                    pasteNameEditText.setText("");
                    pasteContentEditText.setText("");
                } else {
                    mToastText = getResources().getString(R.string.invalid_language);
                }
            } else {
                mToastText = getResources().getString(R.string.no_network);
            }
        } else {
            mToastText = getResources().getString(R.string.paste_no_text);
        }
        Context context = getApplicationContext();
        CharSequence text = mToastText;
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
    }

    /**
     * Sets the title of the ActionBar to be whatever is specified. Called from the various
     * fragments and other activities
     *
     * @param title The title to be used for the ActionBar
     */
    public void setActionBarTitle(String title) {
        //Set the title of the action bar
        //Called from the fragments
        getSupportActionBar().setTitle(title);
    }

    /**
     * AsyncTask for loading the file, from an intent, into the paste content edittext</p>
     * <p/>
     * doInBackground : get the file's contents loaded</p>
     * onPostExecute : update the edittext's content</p>
     */
    class LoadFile extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {
            try {
                mReceivedAction = getIntent().getAction();
                Uri receivedText = mReceivedIntent.getData();
                InputStream inputStream = getContentResolver().openInputStream(receivedText);
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                inputStream.close();
                mFileContents = stringBuilder.toString();
            } catch (IOException e) {
                Log.e(TAG, e.toString());

            }
            return mFileContents;
        }

        protected void onPostExecute(String file) {
            mFileContents = file;
            runOnUiThread(new Runnable() {
                public void run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    EditText pasteContentEditText;
                    pasteContentEditText = (EditText) findViewById(R.id.paste_content_edittext);
                    pasteContentEditText.setText(mFileContents);
                }
            });
        }
    }

    /**
     * ASyncTask that uploads the paste to the selected server</p>
     * <p/>
     * onPreExecute : Setup the upload dialog</p>
     * doInBackground : Perform the upload operation with an HttpClient</p>
     * onPostExecute : Close the dialog box and update the paste url label</p>
     */
    class UploadPaste extends AsyncTask<String, String, String> {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        TextView pasteUrlLabel = (TextView) findViewById(R.id.paste_url_label);
        EditText pasteNameEditText = (EditText) findViewById(R.id.paste_name_edittext);
        String pasteNameString = pasteNameEditText.getText().toString();
        EditText pasteContentEditText = (EditText) findViewById(R.id.paste_content_edittext);
        String pasteContentString = pasteContentEditText.getText().toString();
        final String HTTP_USER_AGENT = "Paste It v" + getString(R.string.version_name) +
                ", an Android app for pasting to Stikked " +
                "(https://play.google.com/store/apps/details?id=org.teamblueridge.pasteitapp)";

        @Override
        protected void onPreExecute() {
            mUploadingText = getResources().getString(R.string.paste_upload);
            super.onPreExecute();
            //Prepare the dialog for uploading the paste
            pDialogUpload = new ProgressDialog(MainActivity.this);
            pDialogUpload.setMessage(mUploadingText);
            pDialogUpload.setIndeterminate(false);
            pDialogUpload.setCancelable(false);
            pDialogUpload.show();
        }

        // post the content in the background while showing the dialog
        protected String doInBackground(String... args) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                UploadDownloadUrlPrep upDownPrep = new UploadDownloadUrlPrep();
                mUploadUrl = upDownPrep.prepUrl(prefs, "upCreate");
                String language = args[0];

                //Ensure username is set, if not, default to "mobile user"
                if (!prefs.getString("pref_name", "").isEmpty()) {
                    mUserName = prefs.getString("pref_name", "");
                } else {
                    mUserName = "Mobile User";
                }
                if (language == null || language.isEmpty()){
                    language = "plaintext";
                }
                //Get ready to actually send everything to the server
                HttpPost httppost = new HttpPost(mUploadUrl);
                // HTTP Header data
                httppost.setHeader("User-Agent", HTTP_USER_AGENT);
                List<NameValuePair> nameValuePairs = new ArrayList<>(2);
                nameValuePairs.add(new BasicNameValuePair("title", pasteNameString));
                nameValuePairs.add(new BasicNameValuePair("text", pasteContentString));
                nameValuePairs.add(new BasicNameValuePair("name", mUserName));
                nameValuePairs.add(new BasicNameValuePair("lang", language));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream in = response.getEntity().getContent();
                StringBuilder stringbuilder = new StringBuilder();
                BufferedReader bfrd = new BufferedReader(new InputStreamReader(in), 1024);
                String line;
                while ((line = bfrd.readLine()) != null)
                    stringbuilder.append(line);
                mPasteUrlString = stringbuilder.toString();
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
            return null;
        }

        //Since we used a dialog, we need to disable it
        protected void onPostExecute(String paste_url) {
            //Dismiss the dialog after getting all products
            pDialogUpload.dismiss();
            //Copy pasteUrl to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("PasteIt", mPasteUrlString);
            clipboard.setPrimaryClip(clip);

            //Display paste URL if allowed in preferences
            runOnUiThread(new Runnable() {
                public void run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    String linkText = "<a href=\"" + mPasteUrlString + "\">" + mPasteUrlString + "</a>";
                    pasteUrlLabel.setText(Html.fromHtml(linkText));
                    pasteUrlLabel.setMovementMethod(LinkMovementMethod.getInstance());
                }
            });
        }
    }

    /**
     * Puts the languages from JSON grabbed via the Stikked API into the Spinner</p>
     * <p/>
     * doInBackground : Read from JSON file, turn JSON into an array of objects and an array of
     * keys.</p>
     * onPostExecute : Populate the spinner with the Array from JSON</p>
     */
    class PopulateSpinner extends AsyncTask<String, String, String[]> {
        ApiHandler apiHandler = new ApiHandler();

        @Override
        protected String[] doInBackground(String... params) {
            //Read the JSON file from internal storage
            return apiHandler.getLanguageArray(getApplicationContext(), "pretty");
        }

        @Override
        protected void onPostExecute(final String[] langListPretty) {
            //Populate spinner
            super.onPostExecute(langListPretty);
            runOnUiThread(new Runnable() {
                public void run() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    String positionListPref = prefs.getString("pref_default_language", "-1");
                    ArrayAdapter<String> uglyList = new ArrayAdapter<>
                            (getApplicationContext(), R.layout.spinner_item, apiHandler
                                    .getLanguageArray(getApplicationContext(), "ugly"));
                    try {
                        Spinner spinner = (Spinner) findViewById(R.id.language_spinner);
                        ArrayAdapter<String> adapter =
                                new ArrayAdapter<>(getApplicationContext(),
                                        R.layout.spinner_item, langListPretty);
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        spinner.setSelection(uglyList.getPosition(positionListPref));
                    } catch (NullPointerException e) {
                        Log.e(TAG, e.toString());
                    }

                }
            });

        }
    }

}