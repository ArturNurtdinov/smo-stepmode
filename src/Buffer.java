import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Buffer {
    private int size;
    private int indexPointer;
    private int maxPriority;
    private ArrayList<Request> requests;

    public Buffer(int size) {
        this.size = size;
        this.maxPriority = BuildConfig.SOURCE_NUMBER;
        indexPointer = 0;
        requests = new ArrayList<>(size);
    }

    public ArrayList<Request> getRequests() {
        return requests;
    }

    public Request get(int packageNumber) {
        double laterTime = 0;
        int index = -1;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i) != null && requests.get(i).getSourceNumber() == packageNumber) {
                laterTime = requests.get(i).getGeneratedTime();
                index = i;
                break;
            }
        }
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i) != null && requests.get(i).getSourceNumber() == packageNumber && requests.get(i).getGeneratedTime() < laterTime) {
                index = i;
            }
        }
        if (index >= 0) {
            Request request = requests.get(index);
            requests.set(index, null);
            return request;
        } else {
            return null;
        }
    }

    public Request get() {
        int index = 0;
        Request priorityRequest = null;
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i) != null) {
                priorityRequest = requests.get(i);
                index = i;
            }
        }
        for (int i = 0; i < requests.size(); i++) {
            Request request = requests.get(i);
            if (requests.get(i) != null && getLessPriority(request.getSourceNumber(), priorityRequest.getSourceNumber()) != request.getSourceNumber()) {
                index = i;
                priorityRequest = requests.get(i);
            }
        }
        Request request = requests.get(index);
        requests.set(index, null);
        return request;
    }

    public int addToBuffer(Request request) {
        if (requests.size() < size) {
            requests.add(indexPointer, request);
            incrementPointer();
            return 0;
        } else {
            int staticPointer = indexPointer;
            for (int i = indexPointer; i < size; i++) {
                if (requests.get(i) == null) {
                    requests.set(i, request);
                    return 0;
                }
                incrementPointer();
            }

            for (int i = 0; i < staticPointer; i++) {
                if (requests.get(i) == null) {
                    requests.set(i, request);
                    return 0;
                }
                incrementPointer();
            }

            // reject request
            int minimalPriority = maxPriority;
            int standaloneIndex = 0;
            Map<Integer, Request> requestsWithLP = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                Request req = requests.get(i);
                if (minimalPriority != getLessPriority(req.getSourceNumber(), minimalPriority)) {
                    minimalPriority = req.getSourceNumber();
                    standaloneIndex = i;
                }
            }
            for (int i = 0; i < size; i++) {
                Request req = requests.get(i);
                if (minimalPriority == req.getSourceNumber()) {
                    requestsWithLP.put(i, req);
                }
            }

            if (minimalPriority == getLessPriority(minimalPriority, request.getSourceNumber())) {
                if (requestsWithLP.values().size() == 1) {
                    requests.set(standaloneIndex, request);
                    indexPointer = standaloneIndex;
                    return 1;
                } else if (requestsWithLP.size() > 0) {
                    Map.Entry<Integer, Request> toReplace = null;
                    for (Map.Entry<Integer, Request> entry : requestsWithLP.entrySet()) {
                        if (toReplace == null || toReplace.getValue().getGeneratedTime() > entry.getValue().getGeneratedTime()) {
                            toReplace = entry;
                        }
                    }

                    requests.set(toReplace.getKey(), request);
                    indexPointer = toReplace.getKey();
                    return 1;
                }
            }
            indexPointer = staticPointer;
            return 2;
        }
    }


    public int getIndexPointer() {
        return indexPointer;
    }

    public boolean isEmpty() {
        return requests.stream().noneMatch(Objects::nonNull) || requests.size() == 0;
    }

    private void incrementPointer() {
        indexPointer++;
        if (indexPointer == size) {
            indexPointer = 0;
        }
    }

    public static void main(String[] args) {
        Buffer buffer = new Buffer(5);
        buffer.addToBuffer(new Request(0, 1, 1));
        buffer.addToBuffer(new Request(2, 2, 2));
        buffer.addToBuffer(new Request(1, 1, 1.5));
        buffer.addToBuffer(new Request(1, 2, 1));
        buffer.addToBuffer(new Request(1, 3, 1.6));
        Main.print(buffer.getRequests() + "\n");
        buffer.addToBuffer(new Request(2, 3, 1));
        Main.print(buffer.getRequests() + "\n");
        buffer.addToBuffer(new Request(0, 3, 3));
        Main.print(buffer.getRequests() + "\n");

        Main.print("");
        //buffer.addToBuffer(new Request(1, 3, 3));
        //buffer.addToBuffer(new Request(1, 4, 4));
        //buffer.addToBuffer(new Request(1, 5, 5));
        Main.print(buffer.get());
        Main.print(buffer.get(2));
        Main.print(buffer.get(2));
        Main.print(buffer.get(2));
        Main.print(buffer.getRequests());
        buffer.addToBuffer(new Request(0, 3, 3));
        Main.print(buffer.getRequests());
        Main.print(buffer.get(1));
        Main.print(buffer.get(1));
        Main.print(buffer.get(0));
        Main.print(buffer.isEmpty());
        Main.print(buffer.getRequests());
    }

    private Integer getLessPriority(Integer first, Integer second) {
        if (first < second) return first;
        else return second;
    }
}
