package org.teamblueridge.pasteitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ApiHandler {

    private static final String TAG = "TeamBlueRidge";

    /**
     *
     * @param context Used in order to be able to read the JSON file
     * @param mPrettyUgly Used in order to identify which String[] to use
     * @return a String[], either pretty or ugly, depending on what is needed
     */
    public String[] getLanguageArray(Context context, String mPrettyUgly) {
        try {
            FileInputStream fis;
            ArrayList<String> langListUgly = new ArrayList<>();
            ArrayList<String> langListPretty = new ArrayList<>();
            fis = context.openFileInput("languages");
            JsonReader reader = new JsonReader(new InputStreamReader(fis, "UTF-8"));
            reader.beginObject();
            while (reader.hasNext()) {
                langListUgly.add(reader.nextName());
                langListPretty.add(reader.nextString());
            }
            reader.endObject();
            String[] languageListUglyStringArray = langListUgly
                    .toArray(new String[langListUgly.size()]);
            String[] languageListPrettyStringArray = langListPretty
                    .toArray(new String[langListPretty.size()]);
            switch (mPrettyUgly) {
                case "pretty":
                    return languageListPrettyStringArray;
                case "ugly":
                    return languageListUglyStringArray;
                default:
                    Log.e(TAG, "Unexpected array description");
                    return null;
            }

        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }

    }

    /**
     * Does some preparation before calling the GetLangs ASyncTask.
     *
     * @param sharedPreferences The shared preferences to be used for determining the domains, etc.
     */
    public void getLanguagesAvailable(SharedPreferences sharedPreferences, Context context) {

        String languageUrl;
        String filename = "languages";

        UploadDownloadUrlPrep upDownPrep = new UploadDownloadUrlPrep();
        languageUrl = upDownPrep.prepUrl(sharedPreferences, "downLangs");

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
            try {
                FileOutputStream outputStream;
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(languageList.getBytes());
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG,e.toString());
                e.printStackTrace();
            }
            return null;
        }

    }
}
