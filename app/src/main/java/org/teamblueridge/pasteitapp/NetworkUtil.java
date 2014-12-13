package org.teamblueridge.pasteitapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class NetworkUtil {

    private static final String TAG = NetworkUtil.class.getName();

    /**
     * @param context the Context
     * @return <code>true</code> if connected to WiFi or a mobile network
     */
    public static boolean isConnectedToNetwork(Context context) {
        final NetworkInfo[] allNetworkInfo = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getAllNetworkInfo();
        NetworkInfo currNetworkInfo;
        boolean anythingConnected = false;
        for (NetworkInfo element : allNetworkInfo) {
            currNetworkInfo = element;
            if (currNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                anythingConnected = true;
            }
        }
        return anythingConnected;
    }

    /**
     * @param context the Context
     * @return <code>true</code> if connected to WiFi
     */
    public static boolean isConnectedToWifi(Context context) {
        final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifi.isConnected();
    }

    /**
     * Reads a file as plain text
     *
     * @param url the URL
     * @return contents of file
     */
    public static String readFromUrl(String url) {
        StringBuilder manifest = new StringBuilder();
        InputStream is;
        try {
            is = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8")));
            int cp;
            while ((cp = rd.read()) != -1) {
                manifest.append((char) cp);
            }
            is.close();
        } catch (IOException e1) {
            Log.d(TAG, "Failed reading url " + url, e1);
        }
        return manifest.toString();
    }
}
