package com.atomiton.smartparking;

import java.util.ArrayList;

import com.atomiton.smartparking.util.SPUtil;

public class GroupSmartParking {
	public static void main(String[] args) {
		System.out.println("Hello group");
		if(args.length > 0){
			System.out.println(args[0]);
			String s = args[0];
			ArrayList<Integer> ints = SPUtil.parseForInts(s);
			for(int i = 0; i < ints.size(); i++){
				System.out.println(ints.get(i));
			}
		}
	}
	
}
