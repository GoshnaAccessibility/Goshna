package com.example.wquist.goshna.Api;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Flight {
    public int id;
    public String airline;
    public String airline_short;
    public String dest_short;
    public int number;
    public String gate;
    public int departure;

    public Flight(int id, String airline, String airline_short, String dest_short, int number, String gate, int departure) {
        this.id = id;
        this.airline = airline;
        this.airline_short = airline_short;
        this.dest_short = dest_short;
        this.number = number;
        this.gate = gate;
        this.departure = departure;
    }

    public String getTime() {
        Date d = new Date(departure * 1000L);
        return new SimpleDateFormat("HH:mm").format(d);
    }

    @Override
    public String toString() {
        return "Flight{" +
                "airline='" + airline + '\'' +
                ", airline_short='" + airline_short + '\'' +
                ", dest_short='" + dest_short + '\'' +
                ", number=" + number +
                ", gate='" + gate + '\'' +
                ", departure=" + departure +
                ", id=" + id +
                '}';
    }
}
