/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.websocket;

import com.pp.currencyfairtest.mtprocessor.TradingHall;
import com.pp.currencyfairtest.mtprocessor.TradingHall.Trader;
import java.io.EOFException;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketEndpoint extends Endpoint {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketEndpoint.class);
    
    volatile Trader trader;
    
    private final MessageHandler.Whole<String> messageHandler = new MessageHandler.Whole<String>() {
        @Override
        public void onMessage(String message) {
            if (trader != null) {
                trader.onMessage(message);
            }
        }
    };
    
    @Override
    public void onOpen(Session session, EndpointConfig config) {
        ServerEndpointConfig serverConfig = (ServerEndpointConfig) config;
        trader = createTrader(new WSClient(session, serverConfig.getPath()));
        session.setMaxTextMessageBufferSize(1000);
        session.addMessageHandler(messageHandler);
    }
    
    Trader createTrader(WSClient client) {
        TradingHall tradingHall = TradingHall.getInstance();
        if (tradingHall == null) {
            return null;
        }
        return tradingHall.createTrader(client);
    }
    
    @Override
    public void onClose(Session session, CloseReason closeReason) {
        TradingHall tradingHall = TradingHall.getInstance();
        if ((tradingHall != null) && (this.trader != null)) {
            tradingHall.destroyTrader(this.trader);
        }
    }
    
    /*
     * OnError can be caused by a user closing her browser. Trying to figure out
     * the cause, and if EOF the it's ignored.
     */
    @Override
    public void onError(Session session, Throwable cause) {
        int causesLeft = 15;
        while ((cause.getCause() != null) && (causesLeft-- > 0)) {
            cause = cause.getCause();
        }
        if (cause instanceof EOFException) {
            // Assuming this was caused by the user when closing her browser.
        } else {
            LOGGER.error("onError(sessionId=" + session.getId() + ": ", cause);
        }
    }
    
}
