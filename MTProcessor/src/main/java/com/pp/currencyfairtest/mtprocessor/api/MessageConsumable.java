package com.pp.currencyfairtest.mtprocessor.api;

import com.pp.currencyfairtest.mtprocessor.dto.Message;

public interface MessageConsumable {
    
    void post(Message message);
    
}
