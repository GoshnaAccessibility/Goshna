package com.example.wquist.goshna.TranslateResponse;

import java.util.List;

public class TextResponse {
    public List<String> text;

    public TextResponse(List<String> text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TextResponse{" +
                "text=" + text +
                '}';
    }
}
