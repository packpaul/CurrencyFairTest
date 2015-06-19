package com.pp.currencyfairtest.mtprocessor.processors;

import com.pp.currencyfairtest.mtprocessor.TradingHall;
import com.pp.currencyfairtest.mtprocessor.api.MessageConsumable;
import com.pp.currencyfairtest.mtprocessor.dto.ProcessingBoard;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class MessageProcessor implements MessageConsumable {
    @Autowired(required = true)
    TradingHall tradingHall;
    
    String endpoint;
    
    public String getEndpoint() {
        return this.endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    /**
     * This method is intended to be called by REST clients
     * @param snapshot  this parameter is interpreted on its own by processor subclasses.
     */
    public abstract ProcessingBoard getBoard(Object snapshot);
}