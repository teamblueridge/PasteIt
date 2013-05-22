package org.teamblueridge.paste;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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


public class MainActivity extends Activity implements OnClickListener {

    Button pasteButton;
    TextView pasteUrlLabel;
    EditText pasteNameEditText;
    String pasteNameString;
    EditText pasteContentEditText;
    String pasteContentString;
    String pasteUrlString;
    String userName;
    
    // Progress Dialog
	private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pasteButton = (Button)findViewById(R.id.button1);
        pasteButton.setOnClickListener(this);
    }
    
    public void onClick(View view){

        pasteUrlLabel=(TextView)findViewById(R.id.textView4);
        pasteNameEditText = (EditText)findViewById(R.id.editText1);
        pasteNameString = pasteNameEditText.getText().toString();
        pasteContentEditText = (EditText)findViewById(R.id.editText2);
        pasteContentString = pasteContentEditText.getText().toString();

        //Execute paste upload in separate thread
        new uploadPaste().execute();
        
        //Clear out the old data in the paste
        pasteNameEditText.setText("");
        pasteContentEditText.setText("");


        //Call toast as pasteUrl is being copied to the clipboard
        Context context = getApplicationContext();
        CharSequence text = "Paste URL has been copied to the clipboard.";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
    }

    public void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(!prefs.getString("pref_name","").isEmpty()){
            userName = prefs.getString("pref_name","");
        }else {
            userName = "Mobile User";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return false;
        }
    }
    //This is the code for the async task used for http traffic
	class uploadPaste extends AsyncTask<String, String, String> {

		//Let the user know that something is happening.
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(MainActivity.this);
			pDialog.setMessage("Uploading Paste. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}
		
		// post the content in the background while showing the dialog
		protected String doInBackground(String... args) {
			HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost("http://paste.teamblueridge.org/api/create");

	        try {
	            // Add your data
	            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	            nameValuePairs.add(new BasicNameValuePair("title", pasteNameString));
	            nameValuePairs.add(new BasicNameValuePair("text", pasteContentString));
	            nameValuePairs.add(new BasicNameValuePair("name", userName));
	            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	            // Execute HTTP Post Request
	            HttpResponse response = httpclient.execute(httppost);
	            InputStream in = response.getEntity().getContent();
	            StringBuilder stringbuilder = new StringBuilder();
	            BufferedReader bfrd = new BufferedReader(new InputStreamReader(in),1024);
	            String line;
	            while((line = bfrd.readLine()) != null)
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
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			
			// finally set the URL for the user
			runOnUiThread(new Runnable() {
				public void run() {
                    //Create a clickable link from pasteUrlString for user (opens in web browser)
                    String linkText = "<a href=\""+pasteUrlString+"\">"+pasteUrlString+"</a>";
                    pasteUrlLabel.setText(Html.fromHtml(linkText));
                    pasteUrlLabel.setMovementMethod(LinkMovementMethod.getInstance());

                }
			});

            //Copy pasteUrl to clipboard
            ClipboardManager clipboard =  (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("TBRPaste", pasteUrlString);
            clipboard.setPrimaryClip(clip);
        }

	}
}
