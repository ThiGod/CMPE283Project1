package com.cmpe283.team03.yangsong;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;

public class AvailabilityManager {
	private static VManager vManager;
	
	public static void main(String[] args) throws Exception {
		vManager = new VManager();
		test();
	}
	
	public static void test() throws Exception {
		BackupManager backupManager = new BackupManager();
		new Thread(backupManager).start();
		
		//BackupManagerForOneHost backupManagerForOneHost = new BackupManagerForOneHost();
		//new Thread(backupManagerForOneHost).start();
		
		//Monitor monitor = new Monitor();
		//new Thread(monitor).run();
		
		//MonitorForOneHost monitorForOneHost = new MonitorForOneHost();
		//new Thread(monitorForOneHost).start();
	}
}
