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

import com.atomiton.smartparking.SmartParking;

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
        System.out.println("why");
        this.session = null;
        this.closeLatch.countDown();
    }
 
    public Map<String, String>parseEvent(String event){
    	Map<String, String> eventMap =  new HashMap<String, String>();
    	
//    	Got event: <Set Name="magneticSensor.parkingSpotId" Target="Atom-Org-1.F2.S193" Time="1424482049254" Value="occupied"/>

    	int startName = event.indexOf("\"")+1;
        int endName = event.indexOf("\"",startName+1);
        String name = event.substring(startName,endName);
        eventMap.put("Name", name);
        
        int startTarget = event.indexOf("\"",endName+1)+1;
        int endTarget = event.indexOf("\"",startTarget+1);
        String target = event.substring(startTarget,endTarget);
        eventMap.put("Target", target);
        
        int startTime = event.indexOf("\"",endTarget+1)+1;
        int endTime = event.indexOf("\"",startTime+1);
        String time = event.substring(startTime,endTime);
        eventMap.put("Time", time);
        
        int startValue = event.indexOf("\"",endTime+1)+1;
        int endValue = event.indexOf("\"",startValue+1);
        String value = event.substring(startValue,endValue);       
    	eventMap.put("Value", value);
    	
    	//Parsing the target:    	
    	//Target="Atom-Org-1.F3.S192"
//    	int orgStart = target.indexOf("")
    	return eventMap;
    }
    
    @OnWebSocketMessage
    public void onMessage(String msg) {
//        System.out.printf("Got event: %s%n", msg);
        Map<String, String>event = parseEvent(msg);
        System.out.println(event.get("Value"));
        switch(event.get("Value")){
        	case "occupied":{
				try {
					SmartParking.updateStallLight(event.get("Value"), "off");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("wassup");
        		break;
        	}
        	case "Car In": {
        		try {
					SmartParking.updateStallLight(event.get("Value"), "on");
				} catch (Exception e) {
					e.printStackTrace();
				}
        		break;
        	}
        	case "Car Out": {
        		try {
					SmartParking.updateStallLight(event.get("Value"), "off");
				} catch (Exception e) {
					e.printStackTrace();
				}
        		break;
        	}
        	case "available": {
        		try {
					SmartParking.updateStallLight(event.get("Value"), "off");
				} catch (Exception e) {
					e.printStackTrace();
				}
        		break;
        	}
        	default:{
        		System.out.println("ERROR");
        		break;
        	}
        }
        
        System.out.println(event.get("Name") + " " + event.get("Target") + " " + event.get("Time") + " " + event.get("Value"));
    }
}
