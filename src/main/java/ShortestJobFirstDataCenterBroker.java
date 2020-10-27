import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.lists.VmList;

import java.util.Comparator;
import java.util.List;

public class ShortestJobFirstDataCenterBroker extends DatacenterBroker {

    public ShortestJobFirstDataCenterBroker(String name) throws Exception {
        super(name);
    }

    protected void submitCloudlets(){
        int vmIndex = 0;
        List<Cloudlet> sortList = getCloudletList();
        sortList.sort((Cloudlet o1, Cloudlet o2) -> (int)(o1.getCloudletLength() - o2.getCloudletLength()));
        for (Cloudlet cloudlet : sortList) {
            Vm vm;
            // if user didn't bind this cloudlet and it has not been executed yet
            if (cloudlet.getVmId() == -1) {
                vm = getVmsCreatedList().get(vmIndex);
            } else { // submit to the specific vm
                vm = VmList.getById(getVmsCreatedList(), cloudlet.getVmId());
                if (vm == null) { // vm was not created
                    Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
                            + cloudlet.getCloudletId() + ": bount VM not available");
                    continue;
                }
            }

            Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
                    + cloudlet.getCloudletId() + " to VM #" + vm.getId()+" Datacenter id: "+getVmsToDatacentersMap().get(vm.getId()));
            cloudlet.setVmId(vm.getId());
            sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
            cloudletsSubmitted++;
            vmIndex = (vmIndex + 1) % getVmsCreatedList().size();
            getCloudletSubmittedList().add(cloudlet);
        }

        // remove submitted cloudlets from waiting list
        for (Cloudlet cloudlet : getCloudletSubmittedList()) {
            getCloudletList().remove(cloudlet);
        }
    }
}
