import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceManager {
    private Device[] devices;
    private int currentPackage = -1;
    private int amountOfDevices;

    public DeviceManager(int amountOfDevices, double alpha, double beta) {
        this.devices = new Device[amountOfDevices];
        this.amountOfDevices = amountOfDevices;
        for (int i = 0; i < devices.length; ++i) {
            devices[i] = new Device(i, alpha, beta);
        }
    }

    public Device get(int index) {
        return devices[index];
    }

    private int getFreeDeviceIndex() {
        ArrayList<Pair<Integer, Device>> freeDevices = new ArrayList<>(amountOfDevices);
        for (int i = 0; i < amountOfDevices; i++) {
            Device device = devices[i];
            if (!device.isBusy()) {
                freeDevices.add(new Pair<>(i, device));
            }
        }

        int lowestPriority = 0;
        if (freeDevices.isEmpty()) {
            throw new IllegalStateException("No free device");
        }
        int freeDeviceIndex = freeDevices.get(0).getFirst();
        for (int i = 0; i < freeDevices.size(); i++) {
            Device device = freeDevices.get(i).getSecond();
            int index = freeDevices.get(i).getFirst();
            if (getMorePriority(device.getNumber(), lowestPriority) != lowestPriority) {
                freeDeviceIndex = index;
                lowestPriority = device.getNumber();
            }
        }

        return freeDeviceIndex;
    }

    public int executeRequest(Request request, double currentTime) {
        int freeDeviceIndex = getFreeDeviceIndex();
        devices[freeDeviceIndex].execute(request, currentTime);
        return freeDeviceIndex;
    }

    public int getCurrentPackage() {
        return currentPackage;
    }


    private Integer getMorePriority(Integer first, Integer second) {
        if (first > second) return first;
        else return second;
    }

    public List<AcceptedRequest> getAcceptedRequests(double currentTime) {
        List<AcceptedRequest> acceptedRequests = new ArrayList<>();
        Arrays.stream(devices)
                .filter(device -> device.getTimeFreed() < currentTime)
                .forEach(device -> {
                    acceptedRequests.add(new AcceptedRequest(device.getNumber(), device.getRequest(), device.getTimeFreed()));
                    device.free();
                });
        return acceptedRequests;
    }

    public void setCurrentPackage(int currentPackage) {
        this.currentPackage = currentPackage;
    }
}
