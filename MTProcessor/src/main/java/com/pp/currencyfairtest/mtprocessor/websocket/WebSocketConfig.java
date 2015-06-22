/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.websocket.Endpoint;
import javax.websocket.server. ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

public class WebSocketConfig implements ServerApplicationConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConfig.class);
    
    private final List<String> endpoints = loadProcessorEndpoints();
    
    static List<String> loadProcessorEndpoints() {
        List<String> processorEndpoints = new ArrayList<>();
        
        SimpleBeanDefinitionRegistry registry = new SimpleBeanDefinitionRegistry();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry);
        reader.loadBeanDefinitions("context.xml");
        
        BeanDefinition beanDef = registry.getBeanDefinition("processors");
        List<BeanDefinitionHolder> processorDefs =
                (List<BeanDefinitionHolder>) beanDef.getPropertyValues().getPropertyValues()[0].getValue();
        
        for (BeanDefinitionHolder processorDef : processorDefs) {
            String endpoint = ((TypedStringValue)processorDef.getBeanDefinition().
                    getPropertyValues().getPropertyValue("endpoint").getValue()).getValue();
            processorEndpoints.add(endpoint);
        }
        
        return processorEndpoints;
    }
    
    @Override
    public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> endpointClasses) {

        Set<ServerEndpointConfig> configs = new HashSet<>();
        
        for (String endpoint : endpoints) {
            LOGGER.debug("Registerning endpoint config for  " + endpoint);
            configs.add(ServerEndpointConfig.Builder.create(
                    WebSocketEndpoint.class, endpoint).build());
        }
        
        return configs;
    }

    @Override
    public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
        return scanned;
    }
    
}
