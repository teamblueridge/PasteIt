package org.teamblueridge.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

public class NetworkUtil {

    private static final String TAG = NetworkUtil.class.getName();

    /**
     * Checks whether the device currently has an active network connection
     *
     * @param context the Context
     * @return true if connected to WiFi or a mobile network
     */
    public static boolean isConnectedToNetwork(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE));
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Reads a file as plain text
     *
     * @param url the URL
     * @return contents of file
     */
    public static String readFromUrl(String url) {
        StringBuilder fetchedApi = new StringBuilder();
        InputStream is;
        try {
            is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8")));
            int cp;
            while ((cp = rd.read()) != -1) {
                fetchedApi.append((char) cp);
            }
            is.close();
        } catch (IOException e1) {
            Log.d(TAG, "Failed reading url " + url, e1);
        }
        return fetchedApi.toString();
    }
}
