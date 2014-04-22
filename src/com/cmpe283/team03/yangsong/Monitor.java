package com.cmpe283.team03.yangsong;

import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;

public class Monitor implements Runnable {
	private static int NUM_OF_RETRY = 6;
	private static boolean RUNNING = true;
	private static boolean PINGABLE = false;
	
	@Override
	public void run() {
		try {
			while(RUNNING) {
				VirtualMachine[] vms = VManager.getAllVMs();
				for(VirtualMachine vm : vms) {
					System.out.println("Ping " + vm.getName() + ": " + Ping.ping(vm.getGuest().getIpAddress()) + ".....");
					if(Ping.ping(vm.getGuest().getIpAddress())) {
						System.out.println(vm.getName() + " works fine!");
						System.out.println();
					} else {
						System.out.println("Fail to ping the vm " + vm.getName());
						if(vm.getRuntime().getPowerState()==VirtualMachinePowerState.poweredOff) {
							System.out.println("Power state: " + vm.getRuntime().getPowerState());
							System.out.println();
						}
						else if(vm.getRuntime().getPowerState()==VirtualMachinePowerState.poweredOn) {
							System.out.println("Power state: " + vm.getRuntime().getPowerState());
							System.out.println("Something wrong with " + vm.getName());
							System.out.println("Try to ping " + vm.getName() + " again!");
							for(int i=0; i<NUM_OF_RETRY; i++) {
								System.out.println((i+1) + " Ping " + ": " + vm.getName() + " : " + Ping.ping(vm.getGuest().getIpAddress()));
								if(Ping.ping(vm.getGuest().getIpAddress())) {
									PINGABLE = true;
									break;
								}
							}
							if(!PINGABLE) {
								VManager.failover(vm);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
