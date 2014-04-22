package com.cmpe283.team03.yangsong;

import java.net.URL;
import java.util.ArrayList;
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


public class Test {
	private static ServiceInstance si;
	private static boolean running = true;
	
	public static void main(String[] args) throws Exception {
		URL url = new URL(MyEntity.VCENTERURL);
		si = new ServiceInstance(url, MyEntity.VCENTERUSERNAME, MyEntity.VCENTERPASSWORD, true);
		//test();
		//backupVM(600000);
		monitor(600000);
	}
	
	public static void test() throws Exception {
		//showVMStatics(si);
		//System.out.println(getAllDatacenters());
		//System.out.println(getAllHosts());
		//System.out.println(getAllVMs());
		//addHost();
		//removeHost(MyEntity.NEWHOSTURL);
		//setPowerOffAlarm();	
	}
	
	public static void monitor(int interval) throws Exception {
		System.out.println("Monitor");
		if (interval <= 0) {
			interval = 600000;
		}
		while(running){
			VirtualMachine[] vms = getAllVMs();
			System.out.println("Start to backup VMs now...");
			for (VirtualMachine vm : vms) {
				createVMSnapshot(vm);
			}
			System.out.println("finished VMs backup. "+ interval/1000 +"sec later will back up again, now waiting....");
			Thread.sleep(interval);			
		}
		while(running) {
			VirtualMachine[] vms = getAllVMs();
			for(VirtualMachine vm : vms) {
				System.out.println("Ping " + vm.getName() + ": " + Test.ping(vm.getGuest().getIpAddress()));
				if(Test.ping(vm.getGuest().getIpAddress())) {
					System.out.println(vm.getName() + " works fine!");
				}
				else if(!Test.ping(vm.getGuest().getIpAddress()) && 
						vm.getRuntime().getPowerState()==VirtualMachinePowerState.poweredOff) {
					System.out.println("Power state: " + vm.getRuntime().getPowerState());
					//powerOnVM(vm);
				}
				else if(!Test.ping(vm.getGuest().getIpAddress()) && 
						vm.getRuntime().getPowerState()==VirtualMachinePowerState.poweredOn) {
					System.out.println("Power state: " + vm.getRuntime().getPowerState());
					System.out.println("Something wrong: " + vm.getName());
					
					//powerOffVM(vm);
					//powerOnVM(vm);
				} else {
					System.out.println("");
					//revertVMSnapshot(vm);
				}
			}
		}
	}
	
	public static void backupVM(int interval) throws Exception {
		if (interval <= 0) {
			interval = 600000;
		}
		while(running){
			VirtualMachine[] vms = getAllVMs();
			System.out.println("Start to backup VMs now...");
			for (VirtualMachine vm : vms) {
				createVMSnapshot(vm);
			}
			System.out.println("finished VMs backup. "+ interval/1000 +"sec later will back up again, now waiting....");
			Thread.sleep(interval);			
		}
	}
	
	public static void showVcenterStatics(ServiceInstance si) throws Exception {
		Folder rootFolder = si.getRootFolder();
		ManagedEntity[] hosts = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
		if(hosts==null || hosts.length==0) {
			return;
		}
		System.out.println("***************************************************");
		for(int h=0; h<hosts.length; h++) {
			System.out.println("Host IP " + (h+1) + ": "+ hosts[h].getName());
			//createVhostSnapshot((HostSystem)hosts[h]);
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
			System.out.println("Guest Power State: " + vmps.name());
			System.out.println("Guest Running State: " + vm.getGuest().guestState);
			System.out.println("Guest IP: " + vm.getGuest().getIpAddress());
			System.out.println("Guest CPU: " + vm.getConfig().getHardware().getNumCPU());
			System.out.println("Guest Memory: " + vm.getConfig().getHardware().getMemoryMB());
			System.out.println("Guest VMTools: " + vm.getGuest().toolsRunningStatus);
			System.out.println("Guest Ping Status: " + ping(vm.getGuest().getIpAddress()));
			System.out.println("---------------------------------------------------");
			//createSnapShot(vm);
		}
		//migrateVM(mes[0].getName(), hosts[1].getName());
	}

	public static Datacenter getDatacenterByName(String name) throws Exception {
		Folder rootFolder = si.getRootFolder();
		Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", name);
		if(dc==null)
			return null;
		else
			return dc;
	}
	
	public static HostSystem getHostByName(String name) throws Exception {
		Folder rootFolder = si.getRootFolder();
		HostSystem vhost = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem", name);
		if(vhost==null)
			return null;
		else
			return vhost;
	}
	
	public static VirtualMachine getVMByName(String name) throws Exception {
		Folder rootFolder = si.getRootFolder();
		VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", name);
		if(vm==null)
			return null;
		else
			return vm;
	}
	
	public static void powerOnVMByName(String name) throws Exception {
		VirtualMachine vm = getVMByName(name);
		Task task = vm.powerOnVM_Task(null);
		System.out.println("Power on " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered on!");
		}
	}
	
	public static void powerOffVMByName(String name) throws Exception {
		VirtualMachine vm = getVMByName(name);
		Task task = vm.powerOffVM_Task();
		System.out.println("Power off " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered off!");
		}
	}
	
	public static void powerOnVM(VirtualMachine vm) throws Exception {
		Task task = vm.powerOnVM_Task(null);
		System.out.println("Power on " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered on!");
		}
	}
	
	public static void powerOffVM(VirtualMachine vm) throws Exception {
		Task task = vm.powerOffVM_Task();
		System.out.println("Power off " + vm.getName() + " in process.....");
		if (task.waitForTask() == Task.SUCCESS) {
			System.out.println("Guest: " + vm.getName() + " powered off!");
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
	
	public static void addNewHost() throws Exception {
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
	
	public static void removeHost(String hostName) throws Exception {
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
	
	public static void createVMSnapshot(VirtualMachine vm) throws Exception {
		String vmSnapshotName = vm.getName() + "-lastest-snapshot";
		Task task = vm.createSnapshot_Task(vmSnapshotName, "", false, false);
		System.out.println("Try to create a snapshot for: " + vm.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("Snapshot for: " + vm.getName() + " created successfully!");
			System.out.println("Snapshot name: " + vmSnapshotName);
		} else {
			System.out.println("Snapshot create failed!");
		}
	}
	
	public static void createVhostSnapshot(HostSystem hs) throws Exception {
		String vhostName = hs.getName();
		String vhostNameInAdmin = MyEntity.VHOSTMAP.get(vhostName);
		//System.out.println(vhostNameInAdmin);
		URL url = new URL("https://130.65.132.14/sdk");
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
 			} else {
 				System.out.println("Snapshot create failed!");
 			}
 		}
	}
	
	public static void revertVMSnapshot(VirtualMachine vm) throws Exception {
		Task task = vm.revertToCurrentSnapshot_Task(null);
		System.out.println("Try to revert snapshot for " + vm.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("Revert snapshot for: " + vm.getName() + " successfully!");
			powerOnVM(vm);
		} else {
			System.out.println("Revert snapshot failed!");
		}
	}
	
	public static void revertVhostSnapshot(HostSystem hs) throws Exception {
		String vhostName = hs.getName();
		String vhostNameInAdmin = MyEntity.VHOSTMAP.get(vhostName);
		//System.out.println(vhostNameInAdmin);
		URL url = new URL("https://130.65.132.14/sdk");
 		ServiceInstance adminSi = new ServiceInstance(url, "administrator", "12!@qwQW", true);
 		Folder rootFolder = adminSi.getRootFolder();
 		String name = rootFolder.getName();
 		System.out.println("root:" + name);
 		VirtualMachine vmInAdmin = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vhostNameInAdmin);
 		if(vmInAdmin==null) {
 			System.out.println("Cannot find: " + hs.getName() + " in admin!");
 		} else {
 			Task task = vmInAdmin.revertToCurrentSnapshot_Task(null);
 			System.out.println("Try to revert snapshot for " + vmInAdmin.getName());
 			if (task.waitForTask() == task.SUCCESS) {
 				System.out.println("Revert snapshot for: " + vmInAdmin.getName() + " successfully!");
 				powerOnVM(vmInAdmin);
 			} else {
 				System.out.println("Revert snapshot failed!");
 			}
 		}
	}
	
	public static void migrateVM(String vMachine, String newHost) throws Exception {
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
	
	public static void failover(VirtualMachine vm) throws Exception {
		
	}
}
