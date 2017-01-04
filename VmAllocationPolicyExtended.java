/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proiect.cc;

import java.util.Date;
import java.util.HashMap;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.*;
import java.util.List;
import java.util.Map;
import org.cloudbus.cloudsim.HostDynamicWorkload;


/**
 *
 * @author IOANA
 */
public class VmAllocationPolicyExtended extends VmAllocationPolicy{
    
        /** The map between each VM and its allocated host.
         * The map key is a VM UID and the value is the allocated host for that VM. */
	private Map<String, Host> vmTable;

	
	/**
	 * Creates a new VmAllocationPolicyExtended object.
	 * 
	 * @param list the list of hosts
	 * @pre $none
	 * @post $none
	 */
	public VmAllocationPolicyExtended(List<? extends Host> list) {
		super(list);
                List<HostDynamicWorkload> hosts = (List<HostDynamicWorkload>) list;
		setVmTable(new HashMap<String, Host>());
	}

	/**
	 * Allocates the host with less resources used for a given VM.
	 * 
	 * @param vm {@inheritDoc}
	 * @return {@inheritDoc}
	 * @pre $none
	 * @post $none
	 */
	@Override
	public boolean allocateHostForVm(Vm vm) {
            System.out.println("--- Allocating virtual machine on host---");
            int ramThreshold = 3072; //3GB threshold for Ram
            int cpuThreshold = 70; //70% threshold for CPU usage
            int bwThreshold = 102400; //100Gb threshold for Bw
            Host host = null;
            boolean result = false;
            List<PowerHost> hosts = getHostList();
            int tries = 0;
            if (!getVmTable().containsKey(vm.getUid())) {
                for (int i = 0; i < hosts.size(); i++) { 
                    if (!hosts.get(i).isSuitableForVm(vm)) {
                          System.out.println("The host " + hosts.get(i).getId() + " is not suitable for VM");
                           if (hosts.size() > 1) {
                               hosts.remove(i);
                           }
			   if (hosts.size() == 1){
                              System.out.println("No more hosts suitable for vm.");
                              return result;
                           }
                    
		    }   
                }  
                

                double maxCpu = hosts.get(0).getUtilizationOfCpu();
                double maxRam = hosts.get(0).getRam()-hosts.get(0).getUtilizationOfRam();
                double maxBw = hosts.get(0).getBw()-hosts.get(0).getUtilizationOfBw();

                System.out.println("Max RAM " + maxRam);
                System.out.println("Max CPU " + maxCpu);
                System.out.println("Max Bw: " + maxBw);
                
                PowerHost hostRam = hosts.get(0);
                PowerHost hostCpu = hosts.get(0);
                PowerHost hostBw = hosts.get(0);
                host = hosts.get(0);
                for (int i = 1;i< hosts.size(); i++) {
                    System.out.println("CPU utilization: "+hosts.get(i).getUtilizationOfCpu());
                    if (hosts.get(i).getUtilizationOfCpu() < maxCpu) {
                        maxCpu = hosts.get(i).getUtilizationOfCpu();
                        hostCpu = hosts.get(i);
                    }

                    if ((hosts.get(i).getRam()-hosts.get(i).getUtilizationOfRam()) > maxRam) {
                        maxRam = (hosts.get(i).getRam()-hosts.get(i).getUtilizationOfRam());
                        hostRam = hosts.get(i);
                    }

                    if ((hosts.get(i).getBw()-hosts.get(i).getUtilizationOfBw()) > maxBw) {
                        maxBw = (hosts.get(i).getBw()-hosts.get(i).getUtilizationOfBw());
                        hostBw = hosts.get(i);
                    }
                    
                }
                do {
                    //if it is not the same host with max values
                    if ( (hostRam.getId() != hostCpu.getId()) || (hostRam.getId() != hostBw.getId()) )
                    {               
                        System.out.println("It is not only one host with good values");

                        if ( vm.getCurrentRequestedRam() >= ramThreshold ) {
                             System.out.println("VM requires RAM more than threshold.");
                             host = hostRam;               
                        } else if (vm.getTotalUtilizationOfCpu(new Date().getTime()) >= cpuThreshold) {
                            System.out.println("VM requires CPU more than threshold.");
                            host = hostCpu;
                        } else if (vm.getCurrentRequestedBw() >= bwThreshold) {
                            System.out.println("VM requires Bw more than threshold.");
                            host = hostBw;
                        } else if (hostRam.getId() == hostCpu.getId() || hostRam.getId() == hostBw.getId()) {
                            System.out.println("One host with best max values for RAM and CPU or Bw.");
                            host = hostRam;
                        } else if (hostCpu.getId() == hostBw.getId()) {
                            System.out.println("One host with best max values for CPU and Bw.");
                            host = hostCpu;
                        } else {
                            System.out.println("Random host chosen.");
                            host = hostRam; // if no condition is accomplished then a random host is chosen
                        }
                    } else {
                        System.out.println("Only one host with good values.");
                        host = hostRam;      
                    }   

                    result = host.vmCreate(vm);
                    if (result) { // if vm were succesfully created in the host
                        getVmTable().put(vm.getUid(), host);
                        result = true;
                    }
                    tries ++;
                } while (!result && tries < 4);
            }  
            
            System.out.println("------Finish allocating virtual machine on host " + host.getId() +  "-----");
            return result;
	
        }
        /**
	 * Allocates the hosts in order to optimize power consumption.
	 * 
	 * @param vm {@inheritDoc}
	 * @return {@inheritDoc}
	 * @pre $none
	 * @post $none
	 */
        
        public boolean allocateHostForVm_optimizePowerConsumption(Vm vm) {
            System.out.println("--- Allocating virtual machine on host---");
            boolean result = false;
            List<PowerHost> hosts = getHostList();
            Host host = null;
            if (!getVmTable().containsKey(vm.getUid())) {
                for (int i = 0; i < hosts.size(); i++) {
                    System.out.println("------Host id: " + hosts.get(i).getId());
                    System.out.println("------Host available mips: " + hosts.get(i).getAvailableMips() +  "-----");
                    System.out.println("------Host available PEs: " + hosts.get(i).getNumberOfFreePes() +  "-----");
                    System.out.println("------Host available RAM: " + hosts.get(i).getRamProvisioner().getAvailableRam() +  "-----");
                    System.out.println("------Host available Bw: " + hosts.get(i).getBwProvisioner().getAvailableBw() +  "-----");
                    if (hosts.get(i).isSuitableForVm(vm)) {
                          System.out.println("The host " + hosts.get(i).getId() + " is suitable for VM");
                          host = hosts.get(i);
                          result = host.vmCreate(vm);
                          break;
                    }
                }  
            }
            
            if (result) { // if vm were succesfully created in the host
                getVmTable().put(vm.getUid(), host);
                result = true;
            }
            if (result) {
                System.out.println("------Finish allocating virtual machine on host " + host.getId() +  "-----");
            } else {
                System.out.println("-----No host available for vm-----");
            }
                
            return result;	
        }
        
	@Override
	public void deallocateHostForVm(Vm vm) {
		Host host = getVmTable().remove(vm.getUid());
                if (host != null) {
			host.vmDestroy(vm);
		}
	}

	@Override
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}

	@Override
	public Host getHost(int vmId, int userId) {
		return getVmTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 * 
	 * @return the vm table
	 */
	public Map<String, Host> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 * 
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, Host> vmTable) {
		this.vmTable = vmTable;
	}

        
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean allocateHostForVm(Vm vm, Host host) {
		return true;
	}
    
}
