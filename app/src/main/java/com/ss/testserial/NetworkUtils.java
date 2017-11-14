package com.ss.testserial;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by goghox on 11/12/17.
 */

public class NetworkUtils {

    private static OkHttpClient httpClient = new OkHttpClient();
    private static String TAG = "TAG";

    /**
     * 会阻塞线程
     * @throws IOException
     */
    public static String checkPwd() throws IOException {

        Request request = new Request.Builder()
                .url("192.168.1.108:8080/locker")
                .build();

        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        Log.i(TAG, "onResponse: "+ json);
        return json;
    }
}
