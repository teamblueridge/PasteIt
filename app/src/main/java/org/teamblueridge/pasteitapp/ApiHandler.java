package org.teamblueridge.pasteitapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
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
     * Gets a list of the languages supported by the remote server
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
            reader.close();
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
        GetLangs getLangs = new GetLangs((Activity)context);
        getLangs.execute(languageUrl, filename, context);

    }

    /**
     * ASyncTask to get the languages from the server and write them to a file, "languages" which
     * can be read later
     */
    public class GetLangs extends AsyncTask<Object, String, String> {
        private Activity activity;

        public GetLangs(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Object... params) {
            String languageUrl = (String) params[0];
            String filename = (String) params[1];
            Context context = (Context) params[2];
            String languageList = NetworkUtil.readFromUrl(languageUrl);
            String validLanguageFile;
            try {
                FileOutputStream outputStream;
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                if (languageList.length() > 0) {
                    outputStream.write(languageList.getBytes());
                    validLanguageFile = "true";
                } else {
                    outputStream.write("{\"text\":\"Plain Text\"}".getBytes());
                    validLanguageFile = "false";
                }
                outputStream.close();

            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
                validLanguageFile = "false";
            }
            return validLanguageFile;
        }

        public void onPostExecute(String valid) {
            boolean isValid = Boolean.parseBoolean(valid);
            if(!isValid) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                        builder1.setMessage("Your API key is incorrect. Please fix it in settings. "
                                + "The only language available for uploads is Plain Text");
                        builder1.setCancelable(true);
                        builder1.setNeutralButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                });
            }
        }
    }
}
