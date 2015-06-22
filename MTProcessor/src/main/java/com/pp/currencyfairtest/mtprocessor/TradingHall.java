/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor;

import com.pp.currencyfairtest.mtprocessor.websocket.WSClient;
import com.pp.currencyfairtest.mtprocessor.websocket.WSClient.WSMessage;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class TradingHall implements InitializingBean, DisposableBean  {
    
    private static volatile TradingHall instance;
    
    private final ExecutorService broadcastingExecutor = Executors.newSingleThreadExecutor();
    
    private final Collection<Trader> traders = new HashSet<>();
    
    /**
     * @return  instance that can be also null
     */
    public static TradingHall getInstance() {
        return instance;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    @Override
    public void destroy() throws Exception {
        instance = null;
        broadcastingExecutor.shutdownNow();
        destroyTraders();
    }

    public Trader createTrader(WSClient client) {
        Trader trader = new Trader(client);
        synchronized(traders) {
            traders.add(trader);
        }

        return trader;
    }
    
    /**
     * This method is intended to be called by different processors
     */
    public void updateTraders(final String endpoint) {
        broadcastingExecutor.execute(new Runnable() { 
            @Override
            public void run() {
                synchronized(traders) {
                    for (Trader trader : traders) {
                        if (trader.isReady() && endpoint.equals(trader.getEndpointPath())) {
                            trader.update();
                        }
                    }
                }        
            }
        });
    }
    
    public void destroyTraders() {
        synchronized(traders) {
            for (Trader trader : traders) {
                trader.close();
            }
            traders.clear();
        }
    }

    public void destroyTrader(Trader trader) {
        trader.close();
        synchronized(traders) {
            traders.remove(trader);
        }
    }

    public static class Trader {
        private static int traderIdCounter;
        
        private final int id;
        
        private volatile boolean isReady;
        
        final WSClient wsClient;
        
        Trader(WSClient wsClient) {
            id = ++traderIdCounter;
            this.wsClient = wsClient;
        }
        
        public void onMessage(String message) {
            if (WSMessage.INIT.toString().equals(message)) {
                if (! isReady) {
                    isReady = true;
                    update();
                }
            }
        }
        
        public int getId() {
            return this.id;
        }
        
        public String getEndpointPath() {
            return wsClient.getEndpointPath();
        }
        
        boolean isReady() {
            return isReady;
        }
        
        void close() {
            wsClient.sendMessage(WSMessage.CLOSE);
        }
        
        void update() {
            wsClient.sendMessage(WSMessage.UPDATE);
        }
        
        @Override
        public boolean equals(Object obj) {
            if ((obj == null) || (getClass() != obj.getClass())) {
                return false;
            }
            final Trader other = (Trader) obj;
            
            return (this.id == other.id);
        }

        @Override
        public int hashCode() {
            return this.id;
        }
    }
    
}
