package org.teamblueridge.pasteitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

public class ApiHandler {


    /**
     * Does some preparation before calling the GetLangs ASyncTask.
     *
     * @param sharedPreferences The shared preferences to be used for determining the domains, etc.
     */
    public void getLanguagesAvailable(SharedPreferences sharedPreferences, Context context) {

        String languageUrl;
        String filename = "languages";
        Log.d("TeamBlueRidge", "Getting languages...");

        UploadDownloadUrlPrep upDownPrep = new UploadDownloadUrlPrep();
        languageUrl = upDownPrep.prepUrl(sharedPreferences, "downLangs");
        Log.d("TeamBlueRidge", "Have correct URL");

        new GetLangs().execute(languageUrl, filename, context);

    }

    /**
     * ASyncTask to get the languages from the server and write them to a file, "languages" which
     * can be read later
     */
    class GetLangs extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            String languageUrl = (String) params[0];
            String filename = (String) params[1];
            Context context = (Context) params[2];
            String languageList = NetworkUtil.readFromUrl(languageUrl);
            Log.d("TeamBlueRidge", "Have list of languages");
            try {
                FileOutputStream outputStream;
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(languageList.getBytes());
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("TeamBlueRidge", "File written");
            return null;
        }

    }
}
