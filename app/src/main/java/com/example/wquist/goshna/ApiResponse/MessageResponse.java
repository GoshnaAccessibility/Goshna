package com.example.wquist.goshna.ApiResponse;

import androidx.annotation.NonNull;

import java.util.List;

import com.example.wquist.goshna.Api.Message;

public class MessageResponse {
    public List<Message> messages;

    public MessageResponse(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * An empty MessageResponse denotes a successful connection, but no messages yet.
     * This constructor should only be used once to demonstrate the successful connection.
     */
    public MessageResponse() {}

    /**
     * Checks if MessageResponse is empty. This would only occur if this Response represents
     * a successful connection to the server, and no messages have been sent by the server yet.
     * @return `true` if empty (no messages), otherwise `false`
     */
    public boolean isEmpty() {
        return messages == null || messages.isEmpty();
    }

    @Override
    @NonNull
    public String toString() {
        return "MessageResponse{" +
                "messages=" + messages +
                '}';
    }
}
