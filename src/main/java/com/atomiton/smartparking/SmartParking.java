/**
 * Copyright © 2015 Atomiton, Inc. All rights reserved.

	This file, or any software and its documentation downloaded from 
	Atomiton website tql.atomiton.com (hereinafter referred as “Software”) 
	is copyright protected work of Atomiton, inc. 
	You may not use, copy, modify or 
	distribute Software without an End User License Agreement (hereinafter referred as “EULA”). 
	Use of Software must be restrained by the clauses in the EULA. 
	You may obtain a copy of the EULA at:
	http://www.atomiton.com
	Unless you agree with the clauses set out in the EULA, you may not install or 
	use any Atomiton Software.
 */
package com.atomiton.smartparking;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.json.JSONObject;

import com.atomiton.smartparking.model.ParkingFloor;
import com.atomiton.smartparking.model.ParkingLot;
import com.atomiton.smartparking.model.ParkingSpot;
import com.atomiton.smartparking.util.HttpRequestResponseHandler;
import com.atomiton.smartparking.util.ParkingLotAction;
import com.atomiton.smartparking.util.SPConstants;
import com.atomiton.smartparking.util.WebSocketListener;



/*
 * The purpose of this class is to generate Events.
 * @author baseerkhan
 *
 */
public class SmartParking {
	
	//global variables
	
	public static Map<String, String> pStallMap;
	/*******************************************************************************
	 * @param args
	 * 
	 * 
	 * 
	 * 
	 ********************************************************************************/
	
	public static void main(String[] args) {
		try {

			System.out.println("Welcome to Smart Parking Hello World Application.");
			
			pStallMap = getStallLightMap(getSnapshot());
			if (args.length > 0) {
				switch (args[0]) {
				case "getOrgs": {
					System.out.println("Getting list of Organization Id..");
					getOrgId();
					break;
				}
				case "snapshot": {
					//Construct the URL to get Parking Lot snapshot
					printSnapshot();
					break;
				}
				
				case "events": {
					WebSocketClient client = new WebSocketClient();
					WebSocketListener wsListener = new WebSocketListener();
					try {
						client.start();
						URI wsUri = new URI(SPConstants.SP_EVENTS_WSURL);
						ClientUpgradeRequest request = new ClientUpgradeRequest();
						client.connect(wsListener, wsUri, request);
						System.out.printf("Connecting to : %s%n", wsUri);
						while(true) {
							ParkingLot pl = getSnapshot();
							for (ParkingFloor pf: pl.getParkingFloors()) {
								int ocRate = getOccupancy(pf);
								updateAreaLights(pf, ocRate);
								updateFloorPrice(pf, ocRate);
							}
						}
//						wsListener.awaitClose(5, TimeUnit.SECONDS);
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						try {
							client.stop();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					break;
				}

				case "updateLight": {
					ParkingLot pl = getSnapshot();
					for (ParkingFloor pf: pl.getParkingFloors()) {
						Map<String, String> areaMap = getAreaLightMap(pf);
						String newIntensity = "90";
						if (args.length > 1) {
							newIntensity = args[1];
						}
						Set<String> keys = areaMap.keySet(); //All the spotIds
						for (String spotId: keys) {
							//Change the intensity of all floors one at a time.
							ParkingLotAction.actionOnAreaLight(areaMap.get(spotId), spotId, newIntensity);
						}
					}
					break;
				}
				
				case "updatePrice": {
					ParkingLot pl = getSnapshot();
					for (ParkingFloor pf: pl.getParkingFloors()) {
						Map<String, String> pMeterMap = getParkingMeterMap(pf);
						String newPrice = "1";
						if (args.length > 1) {
							newPrice = args[1];
						}
						Set<String> keys = pMeterMap.keySet(); //All the spotIds
						for (String spotId: keys) {
							//Change the intensity of all floors one at a time.
							ParkingLotAction.actionOnParkingMeter(pMeterMap.get(spotId), spotId, newPrice);
						}
					}
					break;
				}
				
				case "updateStallLight": {
					ParkingLot pl = getSnapshot();
					Map<String, String> pStallMap = getStallLightMap(pl);
					String powerState = "off";
					if (args.length > 1) {
						powerState = args[1];
					}
					Set<String> keys = pStallMap.keySet(); //All the spotIds
					for (String spotId: keys) {
						//Change the intensity of all floors one at a time.
						ParkingLotAction.actionOnStallLight(pStallMap.get(spotId), spotId, powerState);
						System.out.println("-->" + spotId + " " + pStallMap.get(spotId) + "<--");
					}
					break;
				}
				
				case "updateDigitalLabel": {
					ParkingLot pl = getSnapshot();
					String label = "Lot Full";
					if (args.length > 1) {
						label = args[1];
					}
					for (ParkingFloor pf: pl.getParkingFloors()) {
						ParkingLotAction.actionOnDigitalSignage(pf.getFloorInfo().getId(), label);
					}
					break;
				}

				default: {
					printHelp();
					break;
				}
				}
			} else {
				printHelp();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static String getOrgId() throws Exception {
		System.out.println("Getting list of Organizations..");
		String output = HttpRequestResponseHandler.sendGet(
				SPConstants.SERVER_URL, 
				SPConstants.SP_ORG_PARAMS);
		JSONObject obj = new JSONObject(output);
		System.out.println("Name is: " + obj.getJSONObject("Organization").getJSONObject("Name").getString("Value"));
		String id =  obj.getJSONObject("Organization").getJSONObject("id").getString("Value");
		System.out.println("Org Id is: " + id);
		return id;
	}

	public static ParkingLot getSnapshot() throws Exception {
		String snapURL = SPConstants.SNAPSHOT_URL + getOrgId() + "/" + SPConstants.SNAPSHOT_FILENAME;
		String output = HttpRequestResponseHandler.sendGet(snapURL,null);
		ObjectMapper obj = new ObjectMapper();
		ParkingLot pl = obj.readValue(output, ParkingLot.class);
		return pl;
	}

	private static int getOccupancy(ParkingFloor pf) {
		int numOccupied = 0;
		List<ParkingSpot> psList = pf.getParkingSpots();
		for (ParkingSpot ps: psList) {
			if (ps.getState().equals("on")) numOccupied++; 
		}
		return numOccupied*100/psList.size();
	}

	/**
	 * This method creates a map of Parking Spot where Area Lights are attached.
	 * @param pl
	 * @return
	 */
	public static Map<String, String> getAreaLightMap(ParkingFloor pf) {
		Map<String, String> alMap = new HashMap<String, String>();
		for (ParkingSpot ps: pf.getParkingSpots()) {
			if (ps.getAreaLightInfo() != null) { //Is Area Light attached to the spot?
//				System.out.println(ps.getId() + "---->" + ps.getAreaLightInfo().getid());
				alMap.put(ps.getId(), ps.getAreaLightInfo().getid());
			}
		}
		return alMap;
	}
	
	
	/**
	 * This method creates a map of Parking Spot where Stall Lights are attached.
	 * @param pl
	 * @return
	 */
	public static Map<String, String> getStallLightMap(ParkingLot pl) {
		Map<String, String> alMap = new HashMap<String, String>();
		for (ParkingFloor pf: pl.getParkingFloors()) {
			for (ParkingSpot ps: pf.getParkingSpots()) {
				if (ps.getStallLightInfo() != null) { //Is Stall Light attached to the spot?
//					System.out.println(ps.getId() + "---->" + ps.getStallLightInfo().getid());
					alMap.put(ps.getId(), ps.getStallLightInfo().getid());
				}
			}
		}
		return alMap;
	}
	
	
	/**
	 * This method creates a map of Parking Spot where Parking Meters are attached.
	 * @param pl
	 * @return
	 */
	public static Map<String, String> getParkingMeterMap(ParkingFloor pf) {
		Map<String, String> pmMap = new HashMap<String, String>();
		for (ParkingSpot ps: pf.getParkingSpots()) {
			if (ps.getParkingMeterInfo() != null) { //Is Parking attached to the spot?
//				System.out.println(ps.getId() + "---->" + ps.getAreaLightInfo().getid());
				pmMap.put(ps.getId(), ps.getParkingMeterInfo().getid());
			}
		}
		return pmMap;
	}

	public static void printSnapshot() throws Exception {
		ParkingLot pl = getSnapshot();
		//Read Various values of the lot..
		System.out.println(pl.getOrganization().getName());
		for (ParkingFloor pf: pl.getParkingFloors()) {
			System.out.println("Floor Number: " + pf.getFloorInfo().getFloorNumber());
			for (ParkingSpot ps: pf.getParkingSpots()) {
				System.out.println("Parking Spot id: " + ps.getId());
			}
		}
	}

	private static void printHelp() {
		System.out.println("SmartParking <options>. Where options are:");
		System.out.println("getOrgs");
		System.out.println("snapshot");
		System.out.println("events");
		System.out.println("updateLight");
		System.out.println("updatePrice");
		System.out.println("updateStallLight");
	}
	
	public static void updateStallLight(String spotId, String powerState) throws Exception {
		ParkingLotAction.actionOnStallLight(pStallMap.get(spotId), spotId, powerState);
	}
	
	public static void updateAreaLights(ParkingFloor pf, int ocRate) throws Exception {
		Map<String, String> areaMap = getAreaLightMap(pf);
		int newIntensityDb = ocRate > 10 ? ocRate : 10;
		String newIntensity = Integer.toString(newIntensityDb);
		Set<String> keys = areaMap.keySet(); //All the spotIds
		for (String spotId: keys) {
			//Change the intensity of all area lights on floor one at a time.
			ParkingLotAction.actionOnAreaLight(areaMap.get(spotId), spotId, newIntensity);
		}
	}
	
	public static void updateFloorPrice(ParkingFloor pf, int ocRate) throws Exception {
		Map<String, String> pMeterMap = getParkingMeterMap(pf);
		String newPrice = Integer.toString((ocRate / 20) + 1);
		Set<String> keys = pMeterMap.keySet(); //All the spotIds
		for (String spotId: keys) {
			//Change the price of all parking meters on floor one at a time.
			ParkingLotAction.actionOnParkingMeter(pMeterMap.get(spotId), spotId, newPrice);
		}
	}


}
