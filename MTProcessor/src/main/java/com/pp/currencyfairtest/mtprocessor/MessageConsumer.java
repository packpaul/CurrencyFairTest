package com.pp.currencyfairtest.mtprocessor;

import com.pp.currencyfairtest.mtprocessor.api.MessageConsumable;
import com.pp.currencyfairtest.mtprocessor.dto.Message;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Component;

@Component
@Path("/message")
public class MessageConsumer {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageConsumer.class);
    
    private final AtomicLong messagesConsumed = new AtomicLong(0);
    
    @Autowired(required = true)
    @Qualifier("messageSink")
    private MessageConsumable messageSink;
    
    @GET  
    @Path("/test")  
    @Produces("text/plain")  
    public String test(){  
        return "MessageConsumer WS is working.";
    }
    
    @POST
    @Path("/post")  
    @Consumes("application/json")
    public void post(Message message) {
        message.consumedId = messagesConsumed.incrementAndGet();
        
        LOGGER.debug("Consuming message(consumeId={}): {}", message.consumedId, message);
        
        if (messageSink != null) {
            messageSink.post(message);
        }
    }
    
    public int getMessgesConsumed() {
        return messagesConsumed.intValue();
    }

}