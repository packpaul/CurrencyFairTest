package com.pp.currencyfairtest.mtprocessor.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

public final class MessageUtils {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private MessageUtils() {
    }
    
    static String toJsonString(Message message) throws IOException {
        return JSON_MAPPER.writeValueAsString(message);
    }
    
    static Message fromJsonString(String jsonString) throws IOException {
        return JSON_MAPPER.readValue(jsonString, Message.class);
    }
    
}
