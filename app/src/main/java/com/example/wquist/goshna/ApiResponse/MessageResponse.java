package com.example.wquist.goshna.ApiResponse;

import java.util.List;

import com.example.wquist.goshna.Api.Message;

public class MessageResponse {
    public List<Message> messages;

    public MessageResponse(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "messages=" + messages +
                '}';
    }
}
