package com.atomiton.smartparking.util;

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
        System.out.printf("Got msg: %s%n", msg);
        
        int quote1 = msg.indexOf("\"")+1;
        int quote2 = msg.indexOf("\"",quote1+1);
        int quote3 = msg.indexOf("\"",quote2+1)+1;
        int quote4 = msg.indexOf("\"",quote3+1);
        int quote5 = msg.indexOf("\"",quote4+1)+1;
        int quote6 = msg.indexOf("\"",quote5+1);
        int quote7 = msg.indexOf("\"",quote6+1)+1;
        int quote8 = msg.indexOf("\"",quote7+1);

        String name = msg.substring(quote1,quote2);
        String target = msg.substring(quote3,quote4);
        String time = msg.substring(quote5,quote6);
        String value = msg.substring(quote7,quote8);

        System.out.println(name + " " + target + " " + time + " " + value);
    }
}
