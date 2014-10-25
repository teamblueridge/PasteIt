package org.teamblueridge.pasteit;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    String pasteUrlString;
    String pasteDomain;
    String uploadUrl;
    String uploadingText;
    String userName;
    String toastText;
    String pasteApiKey;
    private ProgressDialog pDialog;
    static final String TEAMBLUERIDGE_APIKEY = "tbrpaste";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set-up the paste fragment and give it a name so we can track it
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PasteFragment(), "PasteFragment")
                    .commit();
        }

        // Set-up up navigation
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getFragmentManager().getBackStackEntryCount();
                if (stackHeight > 0) { // if we have something on the stack (doesn't include the current shown fragment)
                    getActionBar().setHomeButtonEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getActionBar().setDisplayHomeAsUpEnabled(false);
                    getActionBar().setHomeButtonEnabled(false);
                }
            }

        });
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
                openSettings();
                return true;
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Switch to the settings fragment
    public void openSettings() {
        // Only open settings if it's not already open
        try {
            if (!getFragmentManager().findFragmentByTag("SettingsFragment").isVisible()) {

            }
        } catch (NullPointerException e) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new SettingsFragment(), "SettingsFragment")
                    .addToBackStack("SettingsFragment")
                    .commit();
        }

    }

    // Start pasting
    public void doPaste() {
        // Only paste if at the paste fragment
        if (getFragmentManager().findFragmentByTag("PasteFragment").isVisible()) {
            EditText pasteNameEditText = (EditText) findViewById(R.id.editTextPasteName);
            EditText pasteContentEditText = (EditText) findViewById(R.id.editTextPasteContent);
            String pasteContentString = pasteContentEditText.getText().toString();
            if (!pasteContentString.isEmpty()) {
                new uploadPaste().execute();
                toastText = getResources().getString(R.string.paste_toast);
                pasteNameEditText.setText("");
                pasteContentEditText.setText("");
            } else {
                toastText = getResources().getString(R.string.paste_no_text);
            }
            Context context = getApplicationContext();
            CharSequence text = toastText;
            int duration = Toast.LENGTH_SHORT;
            Toast.makeText(context, text, duration).show();
        }
    }

    public void setActionBarTitle(String title){
        getActionBar().setTitle(title);
    }

    class uploadPaste extends AsyncTask<String, String, String> {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        TextView pasteUrlLabel = (TextView) findViewById(R.id.pasteUrlLabel);
        EditText pasteNameEditText = (EditText) findViewById(R.id.editTextPasteName);
        String pasteNameString = pasteNameEditText.getText().toString();
        EditText pasteContentEditText = (EditText) findViewById(R.id.editTextPasteContent);
        String pasteContentString = pasteContentEditText.getText().toString();

        //Let the user know that something is happening.
        @Override
        protected void onPreExecute() {
            uploadingText = getResources().getString(R.string.paste_upload);
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(uploadingText);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // post the content in the background while showing the dialog
        protected String doInBackground(String... args) {
            HttpClient httpclient = new DefaultHttpClient();
            // Ensure that the paste URL is set, if not, default to Team BlueRidge
            if (!prefs.getString("pref_domain", "").isEmpty()) {
                pasteDomain = prefs.getString("pref_domain", "");
            } else {
                pasteDomain = "https://paste.teamblueridge.org";
            }
            // Only set the API key for Team BlueRidge
            if (pasteDomain.equals("https://paste.teamblueridge.org")){
                uploadUrl = pasteDomain + "/api/create?apikey=" + TEAMBLUERIDGE_APIKEY;
            }
            else {
                if (!prefs.getString("pref_api_key", "").isEmpty()) {
                    pasteApiKey = prefs.getString("pref_api_key", "");
                    uploadUrl = pasteDomain + "/api/create?apikey=" + pasteApiKey;
                } else {
                    uploadUrl = pasteDomain + "/api/create";
                }
            }
            if (!prefs.getString("pref_name", "").isEmpty()) {
                userName = prefs.getString("pref_name", "");
            } else {
                userName = "Mobile User";
            }
            HttpPost httppost = new HttpPost(uploadUrl);
            try {
                // HTTP Header data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("title", pasteNameString));
                nameValuePairs.add(new BasicNameValuePair("text", pasteContentString));
                nameValuePairs.add(new BasicNameValuePair("name", userName));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                InputStream in = response.getEntity().getContent();
                StringBuilder stringbuilder = new StringBuilder();
                BufferedReader bfrd = new BufferedReader(new InputStreamReader(in), 1024);
                String line;
                while ((line = bfrd.readLine()) != null)
                    stringbuilder.append(line);
                pasteUrlString = stringbuilder.toString();
            } catch (ClientProtocolException e) {
                Log.d("TeamBlueridge", e.toString());
            } catch (IOException e) {
                Log.d("TeamBlueridge", e.toString());
            }
            return null;
        }

        //Since we used a dialog, we need to disable it
        protected void onPostExecute(String paste_url) {
            //Dismiss the dialog after getting all products
            pDialog.dismiss();
            //Copy pasteUrl to clipboard
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("PasteIt", pasteUrlString);
            clipboard.setPrimaryClip(clip);

            //Display paste URL if allowed in preferences
            runOnUiThread(new Runnable() {
                public void run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    String linkText = "<a href=\"" + pasteUrlString + "\">" + pasteUrlString + "</a>";
                    pasteUrlLabel.setText(Html.fromHtml(linkText));
                    pasteUrlLabel.setMovementMethod(LinkMovementMethod.getInstance());
                }
            });
        }
    }

}
