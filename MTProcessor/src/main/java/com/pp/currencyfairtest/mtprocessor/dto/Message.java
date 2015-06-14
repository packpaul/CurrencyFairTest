package com.pp.currencyfairtest.mtprocessor.dto;

import com.fasterxml.jackson.annotation. JsonIgnore;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="message")
public class Message {
    
    @JsonIgnore
    public long consumedId;
    
    public String userId;
    public String currencyFrom;
    public String currencyTo;
    public double amountSell;
    public double amountBuy;
    public double rate;
    /**
     * --> java.util.Date
     */
    public String timePlaced;
    public String originatingCountry;
    
    public static Message createFromJson(String jsonString) throws IOException {
        return MessageUtils.fromJsonString(jsonString);
    }

    @Override
    public String toString() {
        return toJsonString();
    }
    
    private String toJsonString() {
        try {
            return MessageUtils.toJsonString(this);
        } catch (IOException ex) {
            return String.format("<ERROR: Message(consumedId=%d) -> JSON>", consumedId);
        }        
    }

}