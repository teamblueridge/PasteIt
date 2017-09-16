package org.teamblueridge.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

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
    public static String readFromUrl(Context context, String url) {
        StringBuilder fetchedApi = new StringBuilder();
        InputStream inputStream;
        if (!isHostAvailable(context, url)) return "";
        try {
            inputStream = new URL(url).openStream();
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(inputStream, Charset.forName("UTF-8")));
            int cp;
            while ((cp = rd.read()) != -1) {
                fetchedApi.append((char) cp);
            }
            inputStream.close();
        } catch (IOException e1) {
            Log.d(TAG, "Failed reading url " + url, e1);
        }
        return fetchedApi.toString();
    }

    public static String trimProtocol(final String url) {
        if (url.startsWith("http://")) return url.substring(7, url.length());
        if (url.startsWith("https://")) return url.substring(8, url.length());
        return url;
    }

    /* Modified from https://stackoverflow.com/a/16948322 */
    public static boolean isHostAvailable(Context context, String host) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL(host);   // Change to "http://google.com" for www  test.
                HttpsURLConnection urlc = (HttpsURLConnection) url.openConnection();
                urlc.setConnectTimeout(5 * 1000);          // 10 s.
                urlc.connect();
                if (urlc.getResponseCode() == 200) {        // 200 = "OK" code (http connection is fine).
                    Log.wtf("Connection", "Success !");
                    return true;
                } else {
                    return false;
                }
            } catch (MalformedURLException e1) {
                return false;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }
}