/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.processors;

import com.pp.currencyfairtest.mtprocessor.dto.DelegatingMessageProcessingBoard;
import com.pp.currencyfairtest.mtprocessor.dto.Message;
import com.pp.currencyfairtest.mtprocessor.dto.ProcessingBoard;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

public class DelegatingMessageProcessor extends MessageProcessor implements DisposableBean {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatingMessageProcessor.class);
    
    private static final int MESSAGE_STORE_CAPACITY = 100;
    private final Deque<Message> storedMessages = new LinkedList<>();
    
    private final ExecutorService processingExecutor = Executors.newSingleThreadExecutor();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    
    @Override
    public void destroy() throws Exception {
        processingExecutor.shutdown();
    }
    
    @Override
    public void post(final Message message) {
        processingExecutor.submit(new Runnable() {
            @Override
            public void run() {
                process(message);
            }
        });
    }
    
    /**
     * This method simply puts incoming messages to a queue
     * @param message 
     */
    void process(Message message) {
        LOGGER.debug("Processing message(consumeId={})", message.consumedId);
        rwLock.writeLock().lock();
        try {
            if (storedMessages.size() >= MESSAGE_STORE_CAPACITY) {
                storedMessages.poll();
            }
            storedMessages.add(message);
        } finally {
            rwLock.writeLock().unlock();
        }
        tradingHall.updateTraders(endpoint);
    }

    @Override
    public ProcessingBoard getBoard(Object snapshot) {
        
        final Long snapshotConsumedId = (Long) snapshot;
        
        final long lastConsumedId;
        long firstConsumedId = 0;
        
        final List<Message> snapshotMessages;
        
        rwLock.readLock().lock();
        try {
            Message lastMessage = storedMessages.peekLast();
            lastConsumedId = (lastMessage != null) ? lastMessage.consumedId : 0;
            
            if (snapshotConsumedId == null) {
                snapshotMessages = new ArrayList<>(storedMessages);
            } else {
                snapshotMessages = new ArrayList<>();
                Iterator<Message> mi = storedMessages.descendingIterator();
                while(mi.hasNext()) {
                    Message message = mi.next();
                    if (message.consumedId >= snapshotConsumedId.longValue()) {
                        snapshotMessages.add(message);
                        firstConsumedId = message.consumedId;
                    } else {
                        break;
                    }
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }
        
        DelegatingMessageProcessingBoard board = new DelegatingMessageProcessingBoard();
        board.snapshot = lastConsumedId;
        board.startingSnapshot = firstConsumedId;
        board.messages = snapshotMessages;
        
        return board;
    }
    
}
