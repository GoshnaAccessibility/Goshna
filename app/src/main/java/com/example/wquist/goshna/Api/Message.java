package com.example.wquist.goshna.Api;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    public int flight_id;
    public int id; // Server ID
    public String body;
    public int time;

    public Message(int flight_id, int message_id, String body, int time) {
        this.flight_id = flight_id;
        this.id = message_id;
        this.body = body;
        this.time = time;
    }

    public String getTime() {
        Date d = new Date(time * 1000L); // must multiply by 1000 Long for Epoch/Unix to Android conversion
        return new SimpleDateFormat("HH:mm").format(d);
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "flight_id=" + flight_id +
                ", id=" + id +
                ", body='" + body + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
