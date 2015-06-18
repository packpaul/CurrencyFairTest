package com.pp.currencyfairtest.mtprocessor.processors;

import com.pp.currencyfairtest.mtprocessor.dto.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DelegatingMessageProcessor extends MessageProcessor {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingMessageProcessor.class);
    
    @Override
    public void post(Message message) {
        LOGGER.debug("Processing message(consumeId={})", message.consumedId);
        // TODO: calculate state
        tradingHall.updateTraders(endpoint);
    }
    
}
