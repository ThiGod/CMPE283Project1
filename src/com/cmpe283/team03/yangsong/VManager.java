package com.cmpe283.team03.yangsong;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vmware.vim25.Action;
import com.vmware.vim25.AlarmAction;
import com.vmware.vim25.AlarmSetting;
import com.vmware.vim25.AlarmSpec;
import com.vmware.vim25.AlarmTriggeringAction;
import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.MethodAction;
import com.vmware.vim25.MethodActionArgument;
import com.vmware.vim25.StateAlarmExpression;
import com.vmware.vim25.StateAlarmOperator;
import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.Alarm;
import com.vmware.vim25.mo.AlarmManager;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VManager {
	private static ServiceInstance si;
	public static HashMap<String, String> vhostAndVMMap;
	private static boolean PINGABLE = false;
	
	public VManager() throws Exception {
		vhostAndVMMap = new HashMap<String, String>();
		URL url = new URL(MyEntity.VCENTERURL);
		si = new ServiceInstance(url, MyEntity.VCENTERUSERNAME, MyEntity.VCENTERPASSWORD, true);
		updateVhostAndVMMap();
	}
	
	public static void showStatics() throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		if(hosts==null || hosts.length==0) {
			return;
		}
		
		System.out.println("***************************************************");
		for(int h=0; h<hosts.length; h++) {
			System.out.println("Host IP " + (h+1) + ": "+ hosts[h].getName());
		}
		
		System.out.println("***************************************************");
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		for(int m=0; m<mes.length; m++) {
			VirtualMachine vm = (VirtualMachine) mes[m];
			VirtualMachineConfigInfo vminfo = vm.getConfig();
			VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
			vm.getResourcePool();
			System.out.println("---------------------------------------------------");
			System.out.println("Guest " + (m+1));
			System.out.println("Guest Name: " + vm.getName());
			System.out.println("Guest OS: " + vminfo.getGuestFullName());
			System.out.println("Guest CPU Number: " + vm.getConfig().getHardware().numCPU);
			System.out.println("Guest Memory: " + vm.getConfig().getHardware().memoryMB);
			System.out.println("Guest Power State: " + vmps.name());
			System.out.println("Guest Running State: " + vm.getGuest().guestState);
			System.out.println("Guest IP: " + vm.getGuest().getIpAddress());
			System.out.println("Guest CPU: " + vm.getConfig().getHardware().getNumCPU());
			System.out.println("Guest Memory: " + vm.getConfig().getHardware().getMemoryMB());
			System.out.println("Guest VMTools: " + vm.getGuest().toolsRunningStatus);
			//System.out.println("Guest Ping Status: " + Ping.ping(vm.getGuest().getIpAddress()));
			System.out.println("---------------------------------------------------");
		}
	}
	
	public static void addHost() throws Exception {
		HostConnectSpec addHost = new HostConnectSpec();
		addHost.setHostName(MyEntity.NEWHOSTURL);
		addHost.setUserName(MyEntity.NEWHOSTUSERNAME);
		addHost.setPassword(MyEntity.NEWHOSTPASSWORD);
		addHost.setSslThumbprint(MyEntity.NEWHOSTSSLTHUMBPRINT);
		Datacenter dc = getDatacenter();
		System.out.println("Try to add a new host: " + MyEntity.NEWHOSTURL);
		Task task = dc.getHostFolder().addStandaloneHost_Task(addHost, new ComputeResourceConfigSpec(), true);
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("New host: " + MyEntity.NEWHOSTURL + " added succesfully!");
		} else {
			System.out.println("Fail to add host " + MyEntity.NEWHOSTURL);
		}
	}
	
	public static Datacenter[] getAllDatacenters() throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] entities = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
		if(entities==null || entities.length==0) {
			System.out.println("Cannot find any datacenter!");
			return null;
		}
		Datacenter[] datacenters = new Datacenter[entities.length];
		for(int i=0; i<entities.length; i++) {
			datacenters[i] = (Datacenter) entities[i];
		}
		return datacenters;
	}
	
	public static VirtualMachine[] getAllVMs() throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] entities = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		if(entities==null || entities.length==0) {
			System.out.println("Cannot find any virtualmachine!");
			return null;
		}
		VirtualMachine[] virtualMachines = new VirtualMachine[entities.length];
		for(int i=0; i<entities.length; i++) {
			virtualMachines[i] = (VirtualMachine) entities[i];
		}
		return virtualMachines;
	}
	
	public static HostSystem[] getAllHosts() throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] entities = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		if(entities==null || entities.length==0) {
			System.out.println("Cannot find any hostsystem!");
			return null;
		}
		HostSystem[] hostSystems = new HostSystem[entities.length];
		for(int i=0; i<entities.length; i++) {
			hostSystems[i] = (HostSystem) entities[i];
		}
		return hostSystems;
	}
	
	public static void removeHostByName(String hostName) throws Exception {
		HostSystem hs = getHostByName(hostName);
		Task task = hs.disconnectHost();
		System.out.println("Try to disconnect host: " + hostName);
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Host " + hostName + " disconnected succesfully!");
			ComputeResource cr = (ComputeResource) hs.getParent();
			Task rTask = cr.destroy_Task();
			System.out.println("Try to remove host: " + hostName);
			if (rTask.waitForTask() == rTask.SUCCESS) {
				System.out.println("Host " + hostName + " removed successfuly!");
			} else {
				System.out.println("Fail to remove host: " + hostName);
			}
		} else {
			System.out.println("Fail to disconnect host: " + hostName);
		}
	}
	
	
	public static void migrateVMByName(String vMachine, String newHost) throws Exception {
		VirtualMachine vm = getVMByName(vMachine);
		HostSystem hs = getHostByName(newHost);
		ComputeResource cr = (ComputeResource) hs.getParent();
		Task task = vm.migrateVM_Task(cr.getResourcePool(), hs, VirtualMachineMovePriority.highPriority, VirtualMachinePowerState.poweredOff);
		System.out.println("Try to migrate " + vm.getName() + " to " + hs.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("Migrate virtual machine: " + vm.getName() + " successfully!");
		} else {
			System.out.println("Migrate vm failed!");
		}
	}
	
	public static Datacenter getDatacenter() throws Exception {
		Datacenter dc = null;
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] entities = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter");
		if(entities==null || entities.length==0) {
			return null;
		}
		for(ManagedEntity entity : entities) {
			dc = (Datacenter)entity;
		}
		return dc;
	}
	
	public static Datacenter getDatacenterByName(String name) throws Exception {
		Folder rootFolder = si.getRootFolder();
		Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", name);
		if(dc==null) {
			System.out.println("Fail to find Datacenter: " + name);
			return null;
		}
		else
			return dc;
	}
	
	public static HostSystem getHostByName(String name) throws Exception {
		Folder rootFolder = si.getRootFolder();
		HostSystem vhost = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", name);
		if(vhost==null) {
			System.out.println("Fail to find HostSystem: " + name);
			return null;
		}
		else {
			System.out.println("Success found HostSystem: " + name);
			return vhost;
		}
	}
	
	public static VirtualMachine getVMByName(String name) throws Exception {
		Folder rootFolder = si.getRootFolder();
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", name);
		if(vm==null) {
			System.out.println("Fail to find VirtualMachine: " + name);
			return null;
		}
		else
			return vm;
	}
	
	public static void powerOnVMByName(String name) throws Exception {
		VirtualMachine vm = getVMByName(name);
		Task task = vm.powerOnVM_Task(null);
		System.out.println("Power on " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered on!");
		} else {
			System.out.println("Fail to power on " + vm.getName());
		}
	}
	
	public static void powerOffVMByName(String name) throws Exception {
		VirtualMachine vm = getVMByName(name);
		Task task = vm.powerOffVM_Task();
		System.out.println("Power off " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered off!");
		} else {
			System.out.println("Fail to power off " + vm.getName());
		}
	}
	
	public static void powerOnVM(VirtualMachine vm) throws Exception {
		Task task = vm.powerOnVM_Task(null);
		System.out.println("Power on " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered on!");
		} else {
			System.out.println("Fail to power on " + vm.getName());
		}
	}
	
	public static void powerOffVM(VirtualMachine vm) throws Exception {
		Task task = vm.powerOffVM_Task();
		System.out.println("Power off " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered off!");
		} else {
			System.out.println("Fail to power off " + vm.getName());
		}
	}
	
	public static boolean isVMPowerOn(VirtualMachine vm) throws Exception {
		VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
		return vmps == VirtualMachinePowerState.poweredOn;
	}
	
	public static boolean isVMPowerOff(VirtualMachine vm) throws Exception {
		VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
		return vmps == VirtualMachinePowerState.poweredOff;
	}
	
	public static void updateVhostAndVMMap() throws Exception {
		HostSystem[] hss = getAllHosts();
		for(HostSystem hs : hss) {
			String hsName = hs.getName();
			//System.out.println(hsName);
			VirtualMachine[] vms = hs.getVms();
			for(VirtualMachine vm : vms) {
				String vmName = vm.getName();
				//System.out.println(vmName);
				VManager.vhostAndVMMap.put(vmName, hsName);
			}
		}
	}
	
	public static void failover(VirtualMachine vm) throws Exception {
		PINGABLE = false;
		String vmName = vm.getName();
		String hsName = VManager.vhostAndVMMap.get(vmName);
		HostSystem hs = getHostByName(hsName);
		//System.out.println(hsName);
		if(!Ping.ping(vm.getGuest().getIpAddress())) {
			System.out.println("Try to ping guest failed!");
			System.out.println("Start failover!");
			if(Ping.ping(hsName)) {
				System.out.println("Try to ping guest's parent: " + hsName + " : " + Ping.ping(hsName));
				SnapshotManager.revertVMSnapshot(vm);
			} else {
				System.out.println("Try to ping guest's parent: " + hsName + " : " + Ping.ping(hsName));
				SnapshotManager.revertVhostSnapshot(hs);
			}
		}
		System.out.println();
	}
	
	public static void setPowerOffAlarm() throws Exception {
		AlarmManager alarmMgr = si.getAlarmManager();
		for (VirtualMachine vm : getAllVMs()) {
			AlarmSpec spec = createAlarmSpec("VmPowerOffAlarm." + vm.getName());
			Alarm[] alarms = alarmMgr.getAlarm(vm);
			for (Alarm alarm : alarms) {
				if (alarm.getAlarmInfo().getName().equals(spec.getName())) {
					alarm.removeAlarm();
				}
			}
			alarmMgr.createAlarm(vm, spec);
		}
	}

	private static AlarmSpec createAlarmSpec(String alarmName) {
		AlarmSpec alarmSpec = new AlarmSpec();
		StateAlarmExpression stateAlarmExpression = createStateAlarmExpression();
		AlarmAction alarmAction = createAlarmTriggerAction(createPowerOffAction());
		alarmSpec.setExpression(stateAlarmExpression);
		alarmSpec.setName(alarmName);
		alarmSpec.setDescription("Monitor VM state and trigger some alarm actions");
		alarmSpec.setEnabled(true);
		AlarmSetting alarmSetting = new AlarmSetting();
		alarmSetting.setReportingFrequency(0);
		alarmSetting.setToleranceRange(0);
		alarmSpec.setSetting(alarmSetting);
		return alarmSpec;
	}
	
	private static AlarmTriggeringAction createAlarmTriggerAction(Action action) {
		AlarmTriggeringAction alarmTriggeringAction = new AlarmTriggeringAction();
		alarmTriggeringAction.setYellow2red(true);
		alarmTriggeringAction.setAction(action);
		return alarmTriggeringAction;
	}

	private static StateAlarmExpression createStateAlarmExpression() {
		StateAlarmExpression stateAlarmExpression = new StateAlarmExpression();
		stateAlarmExpression.setType("VirtualMachine");
		stateAlarmExpression.setStatePath("runtime.powerState");
		stateAlarmExpression.setOperator(StateAlarmOperator.isEqual);
		stateAlarmExpression.setRed("poweredOff");
		return stateAlarmExpression;
	}

	private static MethodAction createPowerOffAction() {
		MethodAction methodAction = new MethodAction();
		methodAction.setName("PowerOffVM_Task");
		MethodActionArgument argument = new MethodActionArgument();
		argument.setValue(null);
		methodAction.setArgument(new MethodActionArgument[] { argument });
		return methodAction;
	}
}
