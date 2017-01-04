/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proiect.cc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.HostDynamicWorkload;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 *
 * @author IOANA
 */
public class ProiectCC {

    	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {

            Log.printLine("Starting CloudSimExample2...");

            try {
                // First step: Initialize the CloudSim package. It should be called
                // before creating any entities.
                int num_user = 1;   // number of cloud users
                Calendar calendar = Calendar.getInstance();
                boolean trace_flag = false;  // mean trace events

                // Initialize the CloudSim library
                CloudSim.init(num_user, calendar, trace_flag);

                // Second step: Create Datacenters
                //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
                @SuppressWarnings("unused")
                Datacenter datacenter0 = createDatacenter("Datacenter_0");
                
                //Third step: Create Broker
                DatacenterBroker broker = createBroker();
                int brokerId = broker.getId();

                //Fourth step: Create 10 virtual machines
                vmlist = new ArrayList<Vm>();
        
                //VM description
		int mips = 250;
		long size = 10000; //image size (MB)
		int ram = 1000; //vm memory (MB)
		long bw = 1000;
		int pesNumber = 1; //number of cpus
		String vmm = "Xen"; //VMM name
                for (int i=0; i<20; i++){
		    ram  = ram + 10*i;
		    bw  = bw + 10*i;
                    //create two VMs
                    Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());          
                    //add the VM to the vmList
                    vmlist.add(vm);
         
		}    

                //submit vm list to the broker
                broker.submitVmList(vmlist);


                //Fifth step: Create two Cloudlets
                cloudletList = new ArrayList<Cloudlet>();
                
                //Cloudlet properties  
                pesNumber=1;
                long length = 250000;
                long fileSize = 300;
                long outputSize = 300;
                UtilizationModel utilizationModel = new UtilizationModelFull();

                for (int j=0; j<60; j++){                       
                    Cloudlet cloudlet = new Cloudlet(j, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
                    cloudlet.setUserId(brokerId);
                    cloudletList.add(cloudlet);
                }
               
                //submit cloudlet list to the broker
                broker.submitCloudletList(cloudletList);


                // bind the cloudlets to the vms. This way, the broker
                // will submit the bound cloudlets only to the specific VM
                // every machine will have 3 cloudlets
                for (int k=0; k<60; k++){                       
                    broker.bindCloudletToVm(cloudletList.get(k).getCloudletId(),vmlist.get(k/3).getId());
                }
                // Sixth step: Starts the simulation
                CloudSim.startSimulation();

                // Final step: Print results when simulation is over
                List<Cloudlet> newList = broker.getCloudletReceivedList();
                   
                CloudSim.stopSimulation();

                printCloudletList(newList);
                Log.printLine("CloudSimExample2 finished!");
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.printLine("The simulation has been terminated due to an unexpected error");
            }
        }

        private static Datacenter createDatacenter(String name){

            // Here are the steps needed to create a PowerDatacenter:
            // 1. We need to create a list to store
            //    our machine
            List<PowerHost> hostList = new ArrayList<PowerHost>();
            
            // 2. A Machine contains one or more PEs or CPUs/Cores.
            // In this example, it will have only one core.
            List<Pe> peList = new ArrayList<Pe>();

            int mips = 1000;

            // 3. Create PEs and add these into a list.
            peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

            //4. Create Host with its id and list of PEs and add them to the list of machines
            int ram = 2000; //host memory (MB)
            long storage = 1000000; //host storage
            int bw = 5000;
            
            for(int id = 0; id < 5; id++){
                RamProvisioner ramProvisioner =  new RamProvisionerSimple(ram);
                BwProvisioner bwProvisioner = new BwProvisionerSimple(bw);
                VmScheduler vmScheduler = new VmSchedulerTimeShared(peList);
                PowerModel powerModel =  new PowerModelLinear(1000,50);
                    hostList.add(
                        new PowerHost (id,ramProvisioner,bwProvisioner,storage,peList,vmScheduler,powerModel)  
                    );
                ram = ram + id*2000;
                bw = bw + id*1000;
            }

            // 5. Create a DatacenterCharacteristics object that stores the
            //    properties of a data center: architecture, OS, list of
            //    Machines, allocation policy: time- or space-shared, time zone
            //    and its price (G$/Pe time unit).
            String arch = "x86";      // system architecture
            String os = "Linux";          // operating system
            String vmm = "Xen";
            double time_zone = 10.0;         // time zone this resource located
            double cost = 3.0;              // the cost of using processing in this resource
            double costPerMem = 0.05;		// the cost of using memory in this resource
            double costPerStorage = 0.001;	// the cost of using storage in this resource
            double costPerBw = 0.0;			// the cost of using bw in this resource
            LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

            DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                    arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


            // 6. Finally, we need to create a PowerDatacenter object.
            Datacenter datacenter = null;
            try {
                datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyExtended(hostList), storageList, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return datacenter;
    }

    //We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
    //to the specific rules of the simulated scenario
    private static DatacenterBroker createBroker(){

        DatacenterBroker broker = null;
        try {
                broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
                e.printStackTrace();
                return null;
        }
        return broker;
    }

    /**
     * Prints the Cloudlet objects
     * @param list  list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

                Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                     indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                     indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }
}
