package com.pp.currencyfairtest.mtprocessor;

import com.pp.currencyfairtest.mtprocessor.api.MessageConsumable;
import com.pp.currencyfairtest.mtprocessor.dto.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageMultiplexor implements MessageConsumable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageMultiplexor.class);
    
    private List<? extends MessageConsumable> messageProcessors = Collections.emptyList();

    @Override
    public void post(final Message message) {
        LOGGER.debug("Multiplexing message(consumeId={}) to {} processors",
                message.consumedId, messageProcessors.size());
        for (MessageConsumable processor : messageProcessors) {
            processor.post(message);
        }
    }
    
    public void setMessageProcessors(List<MessageConsumable> messageProcessors) {
        this.messageProcessors = new ArrayList<>(messageProcessors);
    }
    
}
