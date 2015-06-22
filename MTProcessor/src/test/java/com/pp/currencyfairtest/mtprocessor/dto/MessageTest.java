/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.dto;

import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTest {
    
    @Test
    public void testCreateFromJson() throws IOException {
        String jsonString = "{\"userId\": \"134256\", \"currencyFrom\": \"EUR\", \"currencyTo\": \"GBP\", \"amountSell\": 1000, \"amountBuy\": 747.10, \"rate\": 0.7471, \"timePlaced\": \"24­JAN­15 10:27:44\", \"originatingCountry\": \"FR\"}";

        Message message = Message.createFromJson(jsonString);
        
        assertEquals(message.userId, "134256");
        assertEquals(message.currencyFrom, "EUR");
        assertEquals(message.currencyTo, "GBP");
        assertEquals(message.amountSell, 1000.0, 0.0);
        assertEquals(message.amountBuy, 747.10, 0.0);
        assertEquals(message.rate, 0.7471, 0.0);        
        assertEquals(message.timePlaced, "24­JAN­15 10:27:44");
        assertEquals(message.originatingCountry, "FR");
    }
    
    @Test
    public void testToString() {
        Message message = new Message();
        
        message.userId = "134256";
        message.currencyFrom = "EUR";
        message.currencyTo = "GBP";
        message.amountSell = 1000.0;
        message.amountBuy = 747.10;
        message.rate = 0.7471;
        message.timePlaced = "24­JAN­15 10:27:44";
        message.originatingCountry = "FR";
        
        String jsonString = message.toString();
        
        assertEquals("{\"userId\":\"134256\",\"currencyFrom\":\"EUR\",\"currencyTo\":\"GBP\",\"amountSell\":1000.0,\"amountBuy\":747.1,\"rate\":0.7471,\"timePlaced\":\"24­JAN­15 10:27:44\",\"originatingCountry\":\"FR\"}",
                jsonString);
    }
    
}