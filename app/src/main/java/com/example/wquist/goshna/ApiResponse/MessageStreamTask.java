package com.example.wquist.goshna.ApiResponse;

import android.os.AsyncTask;
import android.util.Log;

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

    private ArrayList<HttpURLConnection> connections = new ArrayList<>();

    @Override
    protected Void doInBackground(URL... urls) {
        try {
            for (URL url : urls) {
                Log.d("GoshnaServerMessage", "Connecting");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                connections.add(urlConnection);
                Log.d("GoshnaServerMessage", "Opening Buffered Input Stream27");
                readStream(new BufferedInputStream(urlConnection.getInputStream()));
            }
        } catch (IOException e) {
            Log.e("GoshnaServerMessage", "Error connecting to server", e);
            e.printStackTrace();
        } finally {
            Log.d("GoshnaServerMessage", "Cleaning up url connections");
            // Clean up connections - readStream is blocking (while loop), so fine to close connections here
            for (HttpURLConnection conn : connections) {
                conn.disconnect();
            }
        }
        return null;
    }

    private void readStream(InputStream inputStream) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            publishProgress(null);
            while ((line = reader.readLine()) != null && !this.isCancelled()) {
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
                        Log.d("GoshnaServerMessage", "Received: " + newMessages.toString());
                        publishProgress(new MessageResponse(newMessages));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Log.d("GoshnaServerMessage", "Cleaning up InputStream");
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