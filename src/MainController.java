import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

// Д1ОЗ1 – заполнение буферной памяти «по кольцу»
// Д1ОО2 — приоритет по номеру источника; постановка в буфер
// Д2П1 — приоритет по номеру прибора; выбор ИСТОЧНИКА по приоритету по номеру прибора
// Д2Б5 — приоритет по номеру источника, заявки в пакете. выбор заявки из буфера на обслуживание
// ОР1 — сводная таблица результатов;
// ОД3 — временные диаграммы, текущее состояние
public class MainController {
    private int sourceCount;
    private int deviceCount;
    private int bufferSize;
    private double alpha;
    private double beta;
    private double currentTime;

    private Buffer buffer;
    private SourceManager sourceManager;
    private DeviceManager deviceManager;
    private Map<Integer, Integer> sourceRequestsCount;
    private int replaced = 0;
    private int rejected = 0;

    public MainController() {
        sourceCount = BuildConfig.SOURCE_NUMBER;
        deviceCount = BuildConfig.DEVICE_NUMBER;
        bufferSize = BuildConfig.BUFFER_SIZE;
        alpha = BuildConfig.ALPHA;
        beta = BuildConfig.BETA;
        currentTime = 0;

        sourceManager = new SourceManager(sourceCount);
        deviceManager = new DeviceManager(deviceCount, alpha, beta);
        buffer = new Buffer(bufferSize);
        deviceManager.setCurrentPackage(2);
        sourceRequestsCount = new HashMap<>();
    }

    private void checkFreeDevices() {
        List<AcceptedRequest> acceptedRequests = deviceManager.getAcceptedRequests(currentTime);
        for (AcceptedRequest acceptedRequest : acceptedRequests) {
            Request doneRequest = acceptedRequest.getAcceptedRequest();

            if (doneRequest != null) {
                Main.print("Прибор " + acceptedRequest.getDeviceNumber()
                        + " освободился в " + acceptedRequest.getTimeAccept() +
                        ", номер источника заявки - " + doneRequest.getSourceNumber());
            }
            if (!buffer.isEmpty()) {
                int packageNumber = deviceManager.getCurrentPackage();
                Request requestForDevice;
                if (packageNumber != -1) {
                    requestForDevice = buffer.get(packageNumber);
                    if (requestForDevice == null) {
                        Main.print("Пакетная обработка закончилась, нужно получить новый пакет");
                        requestForDevice = buffer.get();
                        deviceManager.setCurrentPackage(requestForDevice.getSourceNumber());
                    }
                } else {
                    requestForDevice = buffer.get();
                    deviceManager.setCurrentPackage(requestForDevice.getSourceNumber());
                }

                double timeToPlace = Math.max(requestForDevice.getGeneratedTime(), acceptedRequest.getTimeAccept());

                int deviceNumber = deviceManager.executeRequest(requestForDevice, timeToPlace);
                Main.print("Заявка от источника номер " + requestForDevice.getSourceNumber() +
                        " загружена на прибор номер " + deviceNumber + " номер обрабатываемого пакета - " + deviceManager.getCurrentPackage());
            }
        }
    }

    public void start() {
        while (currentTime < BuildConfig.TIME_LIMIT) {
            Pair<Double, Request> nextRequestPair = sourceManager.getNextRequest(currentTime);
            Request nextRequest = nextRequestPair.getSecond();
            currentTime += nextRequestPair.getFirst();
            checkFreeDevices();
            sourceRequestsCount.put(nextRequest.getSourceNumber(), sourceRequestsCount.getOrDefault(nextRequest.getSourceNumber(), 0) + 1);
            Main.print("Источник номер " + nextRequest.getSourceNumber() + " создал заявку в " + nextRequest.getGeneratedTime());

            int status = buffer.addToBuffer(nextRequest);
            if (status == 0) {
                Main.print("Заявка добавлена без удалений");
            } else if (status == 1) {
                Main.print("Заявка попала в буфер, выбив оттуда другую заявку");
                replaced++;
            } else {
                Main.print("Заявка ушла в отказ");
                rejected++;
            }

            Main.print("Состояние системы:");
            showInfo();
        }

        System.out.println("Всего заявок было выбито из буфера: " + replaced);
        System.out.println("Всего отказанных заявок не попало в буффер: " + rejected);
    }

    private void showInfo() {
        System.out.println("Состояние буфера на данный момент (по источникам заявок): ");
        System.out.println(buffer.getRequests().stream().map(request -> {
            if (request == null) {
                return "null";
            } else {
                return request.getSourceNumber();
            }
        }).collect(Collectors.toList()));

        System.out.println("Источники на данный момент сгенерировали: ");
        for (int i = 0; i < sourceCount; i++) {
            System.out.println("Источник " + i + " сгенерировал " + sourceRequestsCount.getOrDefault(i, 0));
        }

        System.out.println("Приборы на данный момент: ");
        for (int i = 0; i < deviceCount; i++) {
            if (deviceManager.get(i).isBusy()) {
                System.out.println("Прибор " + i + " занят, освободится в " + deviceManager.get(i).getTimeFreed());
            } else {
                System.out.println("Прибор " + i + " свободен");
            }
        }

        Main.print("Указатель буффера на " + buffer.getIndexPointer() + " элементе");
    }
}
// buffer, sources, devices, otkaz 2 varianta
// indexPointer