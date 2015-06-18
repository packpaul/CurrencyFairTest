package com.pp.currencyfairtest.mtprocessor.processors;

import com.pp.currencyfairtest.mtprocessor.TradingHall;
import com.pp.currencyfairtest.mtprocessor.api.MessageConsumable;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class MessageProcessor implements MessageConsumable {
    @Autowired(required = true)
    TradingHall tradingHall;
    
    String endpoint;

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
