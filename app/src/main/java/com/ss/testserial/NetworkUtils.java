package com.ss.testserial;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by goghox on 11/12/17.
 */

public class NetworkUtils {

    private static String URL_ROOT = "http://192.168.124.132:8080";
    private static OkHttpClient httpClient = new OkHttpClient();
    private static String TAG = "TAG";

    public static String checkPwd() throws IOException {

        Request request = new Request.Builder()
                .url(URL_ROOT + "/locker")
                //.addHeader("token", Global.token)
                .build();

        Response response = httpClient.newCall(request).execute();
        String json = response.body().string();
        Log.i(TAG, "onResponse: "+ json);
        return json;
    }
    public static String login(String username, String password) throws IOException {
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{ \"name\": \""+username+"\",\n  \"password\": \""+password+"\" }");
        Request request = new Request.Builder()
                .url(URL_ROOT + "/auth")
                .post(body)
                .addHeader("content-type", "application/json")
                .build();

        Response response = httpClient.newCall(request).execute();
        if(response.code() == 200) {
            return response.body().source().readUtf8();
        }
        return null;
    }
}
