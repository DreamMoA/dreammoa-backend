package com.garret.dreammoa.domain.dto.gpt.requestdto;

import java.util.List;

import com.garret.dreammoa.domain.dto.gpt.Message;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChatGPTRequest {
    private String model;
    private List<Message> messages;

    public ChatGPTRequest(String model, String prompt) {
        this.model = model;
        this.messages =  new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }
}