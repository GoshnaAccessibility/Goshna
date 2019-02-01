package com.example.wquist.goshna.ApiResponse;

import android.os.AsyncTask;

import com.example.wquist.goshna.Api.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MessageStreamTask extends AsyncTask<URL, MessageResponse, Void> {

    @Override
    protected Void doInBackground(URL... urls) {
        try {
            for (URL url : urls) {
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                readStream(new BufferedInputStream(urlConnection.getInputStream()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readStream(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.startsWith("data:")) { // Only 'data:' part of SSE is currently used
                    // Example:
                    // data: {"messages":[{"body":"Test 4","flight_id":1,"id":4,"time":1548776086}]}
                    try {
                        JSONObject json = new JSONObject(line.substring(line.indexOf(':') + 2).trim());
                        JSONArray jsonMessages =  json.getJSONArray("messages");
                        ArrayList<Message> newMessages = new ArrayList<>();
                        for(int i = 0; i < jsonMessages.length(); i++) {
                            JSONObject message =  jsonMessages.getJSONObject(i);
                            newMessages.add(new Message(
                                    message.getInt("flight_id"),
                                    message.getInt("id"),
                                    message.getString("body"),
                                    message.getInt("time")));
                        }
                        publishProgress(new MessageResponse(newMessages));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}