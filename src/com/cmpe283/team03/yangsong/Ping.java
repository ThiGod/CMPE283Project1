package com.cmpe283.team03.yangsong;

public class Ping {
	public static boolean ping(String ip) throws Exception {
		String cmd = "";
		if (System.getProperty("os.name").startsWith("Windows")) {
			cmd = "ping -n 1 " + ip;
		} else {
			cmd = "ping -c 1 " + ip;
		}
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();
		if (process.exitValue() == 0) {
			return true;
		} else {
			return false;
		}
	}
}
