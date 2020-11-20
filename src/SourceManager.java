public class SourceManager {

    private Source[] sources;
    private Request[] sourcesRequests;
    private double[] requestsTime;

    public SourceManager(int amountOfSources) {
        this.sources = new Source[amountOfSources];
        this.sourcesRequests = new Request[amountOfSources];
        this.requestsTime = new double[amountOfSources];
        for (int i = 0; i < sources.length; ++i) {
            sources[i] = new Source(i, BuildConfig.FREQUENCY);
        }
    }

    public Pair<Double, Request> getNextRequest(double currentTime) {
        generateRequests(currentTime);
        int minTimeIndex = getMinTimeIndex();
        double minWaitTime = requestsTime[minTimeIndex];
        declineWaitTime(minTimeIndex);
        return new Pair<>(minWaitTime, sourcesRequests[minTimeIndex]);
    }

    private void declineWaitTime(int minTimeIndex) {
        double waitTime = requestsTime[minTimeIndex];
        for (int i = 0; i < sources.length; ++i) {
            requestsTime[i] -= waitTime;
        }
    }

    private int getMinTimeIndex() {
        int minIndex = 0;
        for (int i = 1; i < sources.length; ++i) {
            if (requestsTime[i] < requestsTime[minIndex]) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    private void generateRequests(double currentTime) {
        for (int i = 0; i < sources.length; ++i) {
            if (requestsTime[i] <= 0) {
                Pair<Double, Request> generatedRequest = sources[i].generate(currentTime);
                sourcesRequests[i] = generatedRequest.getSecond();
                requestsTime[i] = generatedRequest.getFirst();
            }
        }
    }

}