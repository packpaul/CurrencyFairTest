/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

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
