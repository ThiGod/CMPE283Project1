package com.cmpe283.team03.yangsong;

import java.util.HashMap;

public class MyEntity {
	public static final String VCENTERURL = "https://130.65.132.150/sdk";
	public static final String VCENTERUSERNAME = "administrator";
	public static final String VCENTERPASSWORD = "12!@qwQW";
	
	public static final String NEWHOSTURL = "130.65.132.155";
	public static final String NEWHOSTUSERNAME = "root";
	public static final String NEWHOSTPASSWORD = "12!@qwQW";
	public static final String NEWHOSTSSLTHUMBPRINT = "EE:2B:25:8F:48:6E:38:5C:B3:DD:B0:87:FD:66:AA:1B:25:DF:B9:7C";
	
	public static final HashMap<String, String> VHOSTMAP = new HashMap<String, String>() {
		{
			put("130.65.132.151", "t03-vHost01-cum1-lab1 _.132.151");
			put("130.65.132.155", "t03-vHost01-cum1-proj1_132.155");
			put("130.65.132.159", "t03-vHost01-cum1-lab2_132.159");
		}
	};
}
