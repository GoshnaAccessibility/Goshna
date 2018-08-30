package com.example.wquist.goshna;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import com.example.wquist.goshna.ApiResponse.UserIdResponse;

public class Goshna extends Application {
    public static final String API_ADDRESS = "http://10.0.2.2:5000";
    public static final String API_URL = "/goshna/api";

    public static final String TRANSLATE_ADDRESS = "https://translate.yandex.net";
    public static final String TRANSLATE_URL = "/api/v1.5";
    public static final String TRANSLATE_KEY = "trnsl.1.1.20180717T093138Z.b05f3ac0d77bd052.6d1c4300b4bccd465bc0858e3bdc020126d4f700";

    private static Context mContext;
    private static boolean mRegistered;

    private static ApiInterface mApi;
    private static TranslateInterface mTranslate;
    private static SharedPreferences mPreferences;

    private static Callback<UserIdResponse> userCallback = new Callback<UserIdResponse>() {
        @Override
        public void success(UserIdResponse response, Response clientResponse) {
            int uid = response.id;
            mPreferences.edit().putInt(mContext.getString(R.string.preferences_user), uid).apply();

            mRegistered = true;
        }

        @Override
        public void failure(RetrofitError error) {
            mRegistered = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        RestAdapter ara = new RestAdapter.Builder()
                .setEndpoint(API_ADDRESS + API_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        mApi = ara.create(ApiInterface.class);

        RestAdapter tra = new RestAdapter.Builder()
                .setEndpoint(TRANSLATE_ADDRESS + TRANSLATE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        mTranslate = tra.create(TranslateInterface.class);

        mPreferences = mContext.getSharedPreferences(mContext.getString(R.string.preferences), Context.MODE_PRIVATE);

        mRegistered = false;
        register();
    }

    public static boolean isRegistered() {
        return mRegistered;
    }

    public static void register() {
        int uid = mPreferences.getInt(mContext.getString(R.string.preferences_user), -1);

        if (uid == -1)
            mApi.createUserId(userCallback);
        else
            mRegistered = true;
    }

    public static ApiInterface getApi() {
        return mApi;
    }

    public static TranslateInterface getTranslator() {
        return mTranslate;
    }

    public static SharedPreferences getPreferences() { return mPreferences; }
}
