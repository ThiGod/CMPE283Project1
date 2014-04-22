package com.cmpe283.team03.yangsong;

import java.net.URL;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class SnapshotManager {
	private static boolean PINGABLE = false;
	
	public static void createVhostSnapshot(HostSystem hs) throws Exception {
		String vhostName = hs.getName();
		String vhostNameInAdmin = MyEntity.VHOSTMAP.get(vhostName);
		//System.out.println(vhostNameInAdmin);
		URL url = new URL("https://130.65.132.13/sdk");
 		ServiceInstance adminSi = new ServiceInstance(url, "administrator", "12!@qwQW", true);
 		Folder rootFolder = adminSi.getRootFolder();
 		String name = rootFolder.getName();
 		System.out.println("root:" + name);
 		VirtualMachine vmInAdmin = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vhostNameInAdmin);
 		if(vmInAdmin==null) {
 			System.out.println("Cannot find: " + hs.getName() + " in admin!");
 		} else {
 			System.out.println("Founded " + hs.getName()); 
 			String vhostSnapshotName = vmInAdmin.getName() + "-lastest-shapshot";
 	 		Task task = vmInAdmin.createSnapshot_Task(vhostSnapshotName, "", false, false);
 			System.out.println("Try to create a snapshot for: " + vmInAdmin.getName());
 			if (task.waitForTask() == task.SUCCESS) {
 				System.out.println("Snapshot for: " + vmInAdmin.getName() + " created successfully!");
 				System.out.println("Snapshot name: " + vhostSnapshotName);
 				System.out.println();
 			} else {
 				System.out.println("Snapshot create failed!");
 				System.out.println();
 			}
 		}
	}
	
	public static void createVMSnapshot(VirtualMachine vm) throws Exception {
		String vmSnapshotName = vm.getName() + "-lastest-snapshot";
		Task task = vm.createSnapshot_Task(vmSnapshotName, "", false, false);
		System.out.println("Try to create a snapshot for: " + vm.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("Snapshot for: " + vm.getName() + " created successfully!");
			System.out.println("Snapshot name: " + vmSnapshotName);
		} else {
			System.out.println("Snapshot create failed!");
			System.out.println();
		}
	}
	
	public static void revertVhostSnapshot(HostSystem hs) throws Exception {
		String vhostName = hs.getName();
		String vhostNameInAdmin = MyEntity.VHOSTMAP.get(vhostName);
		//System.out.println(vhostNameInAdmin);
		URL url = new URL("https://130.65.132.13/sdk");
 		ServiceInstance adminSi = new ServiceInstance(url, "administrator", "12!@qwQW", true);
 		Folder rootFolder = adminSi.getRootFolder();
 		String name = rootFolder.getName();
 		System.out.println("root:" + name);
 		VirtualMachine vmInAdmin = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vhostNameInAdmin);
 		if(vmInAdmin==null) {
 			System.out.println("Cannot find: " + vhostName + " in admin!");
 		} else {
 			Task task = vmInAdmin.revertToCurrentSnapshot_Task(null);
 			System.out.println("Try to revert snapshot for " + vmInAdmin.getName());
 			if (task.waitForTask() == task.SUCCESS) {
 				System.out.println("Revert snapshot for: " + vmInAdmin.getName() + " successfully!");
 				System.out.println();
 				VManager.powerOnVM(vmInAdmin);
 				do {
					PINGABLE = Ping.ping(vhostName);
					System.out.println("Try to ping: " + vhostName + " : " + PINGABLE);
					//count++;
					//if(count==30) break;
				} while(!PINGABLE);
				if(PINGABLE) {
					System.out.println(vmInAdmin.getName() + " is fully powered on!");
					System.out.println();
				}
 				VirtualMachine[] vms = hs.getVms();
 				for(VirtualMachine vm : vms) {
 					PINGABLE = false;
 					VManager.powerOnVM(vm);
 					/*
 					do {
 						PINGABLE = Ping.ping(vm.getGuest().getIpAddress());
 						System.out.println("Try to ping: " + vm.getName() + " : " + PINGABLE);
 						VManager.powerOnVM(vm);
 						//count++;
 						//if(count==30) break;
 					} while(!PINGABLE);
 					if(PINGABLE) {
 						System.out.println(vm.getName() + " is fully powered on!");
 						System.out.println();
 					}
 					*/
 				}
 			} else {
 				System.out.println("Revert snapshot failed!");
 				System.out.println();
 			}
 		}
	}
	
	public static void revertVMSnapshot(VirtualMachine vm) throws Exception {
		Task task = vm.revertToCurrentSnapshot_Task(null);
		System.out.println("Try to revert snapshot for " + vm.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("Revert snapshot for: " + vm.getName() + " successfully!");
			System.out.println();
			VManager.powerOnVM(vm);
			do {
				PINGABLE = Ping.ping(vm.getGuest().getIpAddress());
				System.out.println("Try to ping: " + vm.getName() + " : " + PINGABLE);
				//count++;
				//if(count==30) break;
			} while(!PINGABLE);
			if(PINGABLE) {
				System.out.println(vm.getName() + " is fully powered on!");
			}
		} else {
			System.out.println("Revert snapshot failed!");
			System.out.println();
		}
	}
}
