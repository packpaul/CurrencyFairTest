/*
 * Copyright 2015 by Pavel Perminov (packpaul@mail.ru)
 * All code below is exclusively owned by its author - Pavel Perminov.
 * Any changes, modifications, borrowing and adaptation are a subject for
 * explicit permition from owner.
 */

package com.pp.currencyfairtest.mtprocessor.websocket;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a kind of facade to communicate with the socket
 */
public class WSClient {
    
    static final Logger LOGGER = LoggerFactory.getLogger(WSClient.class);
    
    private static final int BUFFER_CAPACITY = 1000;
    static final int MAX_TRYS_TO_RESEND = 5;
    private static final int SCHEDULED_TIMER_DELAY = 100;
    
    final Session session;
    private final String endpointPath;
    
    final Queue<WSMessage> buffer = new LinkedList<>();
    
    private boolean isSending;
    int sentMessagesCount;
    volatile int trysToResend;
    
    volatile boolean isClosing;
    
    private final Timer sendScheduleTimer = new Timer();
    
    /**
     * SendHandler that will continue to send buffered messages.
     */
    private final SendHandler sendHandler = new SendHandler() {
        @Override
        public void onResult(SendResult result) {
            if (! result.isOK()) {
                if (++trysToResend > MAX_TRYS_TO_RESEND) {
                    CloseReason closeReason = new CloseReason(CloseCodes.NOT_CONSISTENT, "Cannot resend message to client!");
                    try {
                        session.close(closeReason);
                    } catch (IOException ex) {
                        LOGGER.error(String.format("Closng on resend failed! (SessionId=%d)", session.getId()), ex);
                    }
                    sendDelayed();
                    return;
                }
            }
            
            if (isClosing) {
                try {
                    session.close();
                } catch (IOException ex) {
                    LOGGER.error(String.format("Closing failed! (SessionId=%d)", session.getId()), ex);
                }
                return;
            }
            
            trysToResend = 0;
            synchronized(buffer) {
                int toPoll = sentMessagesCount;
                while(toPoll-- > 0) {
                    buffer.poll();
                }
                if (! buffer.isEmpty()) {
                    sendDelayed();
                } else {
                    isSending = false;
                }
            }
        }
    };
   
    WSClient(Session session, String endpointPath) {
        this.session = session;
        this.endpointPath = endpointPath;
    }
    
    public void sendMessage(WSMessage message) {
        synchronized(buffer) {
            if (isClosing) {
                return;
            }
            if (buffer.peek() == message) {
                // Let's not repeat the same messages several times in the same parcel
                return;
            }
            
            buffer.add(message);
            
            if (isSending) {
                if (buffer.size() >= BUFFER_CAPACITY) {
                    isClosing = true;
                    CloseReason closeReason = new CloseReason(CloseCodes.TOO_BIG, "Buffer size exeeded!");
                    try {
                        session.close(closeReason);
                    } catch (IOException ex) {
                        LOGGER.error(String.format("Closing on exeeding buffer size failed! (SessionId=%d)", session.getId()), ex);
                    }
                    return;
                }
                return;
            }
            
            isSending = true;
        }
        
        sendInstantly();
    }

    void sendInternal() {
        if (! session.isOpen()) {
            return;
        }
        
        String messageToSend = null;
        
        synchronized(buffer) {
            this.sentMessagesCount = 0;
            int leftBufferSize = session.getMaxTextMessageBufferSize();
            StringBuilder sb = new StringBuilder();

            for (WSMessage message : buffer) {
                int messageSizeInBytes = 2 * (sb.length() + 1); // We reserve 1 for ';'
                if (leftBufferSize < messageSizeInBytes) {
                    break;
                }
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(message.toString());
                sentMessagesCount++;
                
                if (message == WSMessage.CLOSE) {
                    isClosing = true;
                    break;
                }
            }
            
            messageToSend = sb.toString();
        }
        
        if (messageToSend != null) {
            Async remote = session.getAsyncRemote();
            remote.sendText(messageToSend, this.sendHandler);
        }
        
    }
    
    void sendDelayed() {
        sendScheduleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendInternal();
            }
        }, SCHEDULED_TIMER_DELAY);
    }
    
    void sendInstantly() {
        sendScheduleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendInternal();
            }
        }, 0);
    }
    
    public String getEndpointPath() {
        return endpointPath;
    }
    
    public enum WSMessage {
        INIT, // <--
        UPDATE, // -->
        CLOSE // -->
    }   
    
}