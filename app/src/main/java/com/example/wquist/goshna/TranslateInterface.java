package com.example.wquist.goshna;

import com.example.wquist.goshna.TranslateResponse.TextResponse;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface TranslateInterface {
    @GET("/tr.json/translate")
    void translate(@Query("key") String key, @Query("text") String text, @Query("lang") String lang, Callback<TextResponse> response);
}
