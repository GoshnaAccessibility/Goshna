package com.example.wquist.goshna.ApiResponse;

public class UserIdResponse {
    public int id;

    public UserIdResponse(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "UserIdResponse{" +
                "id=" + id +
                '}';
    }
}
