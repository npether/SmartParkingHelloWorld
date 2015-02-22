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
package com.atomiton.smartparking.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * This class provides generic utility functions; not already provided by OdaLib.
 * OdaLib is used whenever appropriate.
 * @author baseerkhan
 *
 */
public class SPUtil {
	
	public static int getRandomNumberBetween(int low, int high) {
		Random random = new Random();
		int randomNumber = random.nextInt(high - low) + low;
		return randomNumber;
	}

	
	public static String readContentsFromFile(String fName) throws Exception {
		File f = new File (fName);
		String strFileContent = FileUtils.readFileToString(f);
		return strFileContent;
	}

	public static void writeContentToFile(String folder, String content, String fName) 
			throws Exception {
		File f = new File (folder);
		if (!f.exists()) {
			f.mkdirs();
		}
		File f2 = new File (folder + "/" + fName);
		FileUtils.writeStringToFile(f2, content, false);
	}
	
	public static void writeContentToFile(String folder, byte[] content, String fName) 
			throws Exception {
		File f = new File (folder);
		if (!f.exists()) {
			f.mkdirs();
		}
		File f2 = new File (folder + "/" + fName);
		FileUtils.writeByteArrayToFile(f2, content);
	}

	public static Date getTodaysDate() {
		Date todaysDate = null;
		try {
			Calendar currentDate = Calendar.getInstance(); //Get the current date
			SimpleDateFormat formatter= new SimpleDateFormat("yyyy-M-dd hh:mm:ss"); //format it as per your requirement
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			String dateNow = formatter.format(currentDate.getTime());
			todaysDate = formatter.parse(dateNow);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return todaysDate;
	}

	public static Long toUTC(Date cDate) {
		long milliSecondsSinceEpoch = cDate.getTime();
		return new Long(milliSecondsSinceEpoch);
	}

	public static long getUTCTime() {
		return toUTC(getTodaysDate()).longValue();
	}

		
	public static boolean isNull(String str) {
		return (str == null || str.length() == 0);
	}
	
	public static Map<String, String>  parseMSEvent(String event) {
		Map<String, String> eventMap =  new HashMap<String, String>();
		
		try {
			DocumentBuilderFactory factory =
			DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document d = builder.parse(
					new ByteArrayInputStream(
							event.getBytes(StandardCharsets.UTF_8)));
			NodeList nList = d.getElementsByTagName("Set");
			Node nNode = nList.item(0);
			Element nm = (Element) nNode;
			
			eventMap.put("Name", nm.getAttribute("Name"));
			eventMap.put("Target", nm.getAttribute("Target"));
			eventMap.put("Value", nm.getAttribute("Value"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eventMap;
		}
	
	public static ArrayList<Integer> parseForInts(String s){
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		for(int i = 0; i < s.length(); i++){
			char ch = s.charAt(i);
			if(Character.isDigit(ch)){
				int j = i;
				while((j < s.length()) && (Character.isDigit(s.charAt(j)))){
					j++;
				}
			numbers.add(Integer.valueOf(s.substring(i, j)));
			i = j;	
			}
		}
		return numbers;
	}
	
	public static String newStatus(String value){
		switch(value){
		case "occupied": return "on";
		case "Car In": return "on";
		case "available": return "off";
		case "Car Out": return "off";
		default: return "off";
		}
	}
	
	public static Map<String, Integer>parseTarget(String target){
		Map<String, Integer> targetMap =  new HashMap<String, Integer>();

		ArrayList<Integer> ids = parseForInts(target);
		
		targetMap.put("Org", ids.get(0));
		targetMap.put("F", ids.get(1));
		targetMap.put("S", ids.get(2));		
		return targetMap;
	}
	
}
