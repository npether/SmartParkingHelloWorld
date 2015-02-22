package com.atomiton.smartparking.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * The purpose of this class is to listen to event over a websockets.
 * Following Events will be recieved: 
 *  Magnetic Sensor Events
 *  Camera Events 
 * @author baseerkhan
 *
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketListener {
 
    @SuppressWarnings("unused")
    private Session session;
 
    private final CountDownLatch closeLatch;
    
    
    public WebSocketListener() {
    	 this.closeLatch = new CountDownLatch(1);
    }
 
    public boolean awaitClose(int duration, TimeUnit unit) 
    		throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }
    
    
    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.printf("Got connect: %s%n", session);
    }
 
 
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        this.closeLatch.countDown();
    }
    
    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.printf("Got event: %s%n", msg);
        Map<String, String>event = SPUtil.parseMSEvent(msg);
        Map<String, Integer>target = SPUtil.parseTarget(event.get("Target"));
                
        System.out.println(event.get("Name") + " " + event.get("Target") + " " + event.get("Time") + " " + event.get("Value"));
        System.out.println(target.get("Org") + " " + target.get("F") + " " + target.get("S"));
    }
}
