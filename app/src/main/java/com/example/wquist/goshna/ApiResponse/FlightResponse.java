package com.example.wquist.goshna.ApiResponse;

import java.util.List;

import com.example.wquist.goshna.Api.Flight;

public class FlightResponse {
    public List<Flight> flights;

    public FlightResponse(List<Flight> flights) {
        this.flights = flights;
    }

    @Override
    public String toString() {
        return "FlightResponse{" +
                "flights=" + flights +
                '}';
    }
}
