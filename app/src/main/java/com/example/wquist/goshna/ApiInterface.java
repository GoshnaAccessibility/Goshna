package com.example.wquist.goshna;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.POST;

import com.example.wquist.goshna.ApiResponse.*;

public interface ApiInterface {
    @GET("/flights")
    void getAllFlights(Callback<FlightResponse> response);

    @GET("/flights/find/{gate}")
    void findFlight(@Path("gate") String gate, Callback<FlightResponse> response);

    @GET("/flights/messages/{flight_id}")
    void getFlightMessages(@Path("flight_id") int flight_id, Callback<MessageResponse> response);

    @POST("/user")
    void createUserId(Callback<UserIdResponse> response);
}
