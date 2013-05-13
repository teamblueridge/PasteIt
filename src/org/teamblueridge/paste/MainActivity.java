package org.teamblueridge.paste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.view.Menu;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;


public class MainActivity extends Activity implements OnClickListener {

	Button pasteButton;
    TextView pasteUrlLabel;
    EditText editText1;
    String pasteName;
    EditText editText2;
    String pasteContents;
    String downloadedString= null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //TODO: Split network connection into separate thread rather than disable strict mode
    	//Edits Android's policies to allow a network connection on main thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    	StrictMode.setThreadPolicy(policy);
 
    	pasteButton = (Button)findViewById(R.id.button1);
        pasteButton.setOnClickListener(this);
    }
    public void onClick(View view){
        
        pasteUrlLabel=(TextView)findViewById(R.id.textView4);
        editText1 = (EditText)findViewById(R.id.editText1);
        pasteName = editText1.getText().toString();
        editText2 = (EditText)findViewById(R.id.editText2);
        pasteContents = editText2.getText().toString();
        
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://paste.teamblueridge.org/api/create");

        try {
        	// Add your data
        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        	nameValuePairs.add(new BasicNameValuePair("title", pasteName));
        	nameValuePairs.add(new BasicNameValuePair("text", pasteContents));
        	nameValuePairs.add(new BasicNameValuePair("name", "Mobile User"));
        	httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        	// Execute HTTP Post Request
        	HttpResponse response = httpclient.execute(httppost);
        	InputStream in = response.getEntity().getContent();
        	StringBuilder stringbuilder = new StringBuilder();
        	BufferedReader bfrd = new BufferedReader(new InputStreamReader(in),1024);
        	String line;
        	while((line = bfrd.readLine()) != null)
        		stringbuilder.append(line);
        		downloadedString = stringbuilder.toString();
        } catch (ClientProtocolException e) {
        		// TODO Auto-generated catch block
        } catch (IOException e) {
 		        // TODO Auto-generated catch block
        }
 		    
        pasteUrlLabel.setText(downloadedString);
        editText1.setText("");
        editText2.setText("");
  	 
	}

    

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        //We don't want the menu display...yet
        //To enable the menu, return true
        return false;
    }
    
}
