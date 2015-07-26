package com.star.networkbestpractice;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtils {

    public static void sendHttpRequest(final String address, final HttpCallbackListener listener) {

        if (!isNetworkAvailable()) {
            Toast.makeText(MyApplication.getContext(), "network is unavailable",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection httpURLConnection = null;

                try {
                    URL url = new URL(address);

                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    httpURLConnection.setRequestMethod(MainActivity.GET_METHOD);
                    httpURLConnection.setConnectTimeout(MainActivity.TIME_OUT);
                    httpURLConnection.setReadTimeout(MainActivity.TIME_OUT);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);

                    InputStream inputStream = httpURLConnection.getInputStream();

                    BufferedReader reader = new BufferedReader( new InputStreamReader(inputStream));

                    StringBuilder response = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    if (listener != null) {
                        listener.onFinish(response.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    if (listener != null) {
                        listener.onError(e);
                    }
                } finally {
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                }
            }
        }).start();

    }

    private static boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager)
                MyApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if (networkInfo != null) {
                for (int i = 0; i < networkInfo.length; i++) {
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

}
