/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor;

import com.pp.currencyfairtest.mtprocessor.dto.ProcessingBoard;
import com.pp.currencyfairtest.mtprocessor.processors.MessageProcessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Path("/processors")
public class ProcessorBillboard implements ApplicationContextAware {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorBillboard.class);
    
    private final Map<String, MessageProcessor> processorsMap = new HashMap<>();
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Object processors = applicationContext.getBean("processors");
        setMessageProcessors((List) processors);
    }

    /**
     * For some reason the spring autowiring didn't work with util:list with this component
     * hence it's set over application {@link setApplicationContext()}
     * @param processors 
     */
//    @Autowired
//    @Qualifier("processors")
    private void setMessageProcessors(List<MessageProcessor> processors) {
        for (MessageProcessor processor : processors) {
            String endpoint = processor.getEndpoint();
            int i = endpoint.lastIndexOf("/");
            String processorId = endpoint.substring(++i);
            MessageProcessor assertionProc = processorsMap.put(processorId, processor);
            if (assertionProc != null) {
                throw new IllegalArgumentException(
                        "There seems to be more than one message processor with the same endpoint: " + endpoint);
            }
        }
        
        LOGGER.debug("Message processors were set.");
    }
    
    @GET  
    @Path("/test")  
    @Produces("text/plain")  
    public String test(){  
        return "ProcessorBillboard Service is working.";
    }
    
    @GET  
    @Path("/{processorId}")
    @Produces("application/json")  
    public ProcessingBoard getBillboard(@PathParam("processorId") String processorId,
                                        @Context HttpServletResponse servlerResponse) {
        return getBillboard(processorId, null, servlerResponse);
    }
    
    @GET  
    @Path("/{processorId}/{snapshot}")  
    @Produces("application/json")  
    public ProcessingBoard getBillboard(
            @PathParam("processorId") String processorId, @PathParam("snapshot") Long snapshot,
            @Context HttpServletResponse servlerResponse) {
        
        // The response header is set to enable testing on localhost
        servlerResponse.addHeader("Access-Control-Allow-Origin", "*");
        
        MessageProcessor processor = processorsMap.get(processorId);
        if (processor == null) {
            LOGGER.warn("No processor (id={}) is registered!", processorId);
            return null;
        }
        
        return processor.getBoard(snapshot);
    }
    
    @GET  
    @Path("/{processorId}/{snapshot}/{params}")  
    @Produces("application/json")  
    public ProcessingBoard getBillboard(
            @PathParam("processorId") String processorId,
            @PathParam("snapshot") Long snapshot, @PathParam("params") Object params,
            @Context HttpServletResponse servlerResponse) {
        
        // The response header is set to enable testing on localhost
        servlerResponse.addHeader("Access-Control-Allow-Origin", "*");
        
        MessageProcessor processor = processorsMap.get(processorId);
        if (processor == null) {
            LOGGER.warn("No processor (id={}) is registered!", processorId);
            return null;
        }
        
        return processor.getBoard(new Object[] {snapshot, params});
    }

}
