package com.cmpe283.team03.yangsong;

import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.VirtualMachine;

public class BackupManager implements Runnable {
	private static boolean RUNNING = true;
	private static int INTERVAL = 600000;
	
	@Override
	public void run() {
		System.out.println("Backup start.....");
		try {
			while(RUNNING) {
				VManager.showStatics();
				//Test.setPowerOffAlarm();
				HostSystem[] hss = VManager.getAllHosts();
				System.out.println("Backuping vHosts.....");
				for(HostSystem hs  : hss) {
					if(Ping.ping(hs.getName())) {
						SnapshotManager.createVhostSnapshot(hs);
					}	
				}
				System.out.println("Finish backup all live vHosts!");
				System.out.println();
				
				VirtualMachine[] vms = VManager.getAllVMs();
				System.out.println("Backuping VMs.....");
				for(VirtualMachine vm : vms) {
					if(Ping.ping(vm.getGuest().getIpAddress())) {
						SnapshotManager.createVMSnapshot(vm);
					}	
				}
				System.out.println("Finish backup all live VMs!");
				System.out.println();
				Thread.sleep(INTERVAL);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
