import java.util.HashMap;

public class Question implements Comparable<Question> {
    private HashMap<String, Boolean> WorkerIdToSelectionMap = new HashMap<String, Boolean>();
    private double workersCount = 0;
    private double consistentWorkersCount = 0;
    private double totalAnswersCount = 0;
    private double positiveAnswersCount = 0;
    private double consistentAnswersCount = 0;
    private double consistentPositiveAnswersCount = 0;
    private String imageName;
    private int duplicateWorkersCount = 0;

    public void addAnswer(String workerId,String imageName, boolean improved) {
        this.imageName = imageName;
        if (WorkerIdToSelectionMap.containsKey(workerId)) {
            // second visit
            if ((improved && WorkerIdToSelectionMap.get(workerId) || (!improved && !WorkerIdToSelectionMap.get(workerId)))) {
                // consistent
                consistentWorkersCount++;
                consistentAnswersCount++;
                consistentPositiveAnswersCount += (improved ? 1 : 0);
            }
            WorkerIdToSelectionMap.remove(workerId);
            duplicateWorkersCount ++;
        } else {
            // first visit
            WorkerIdToSelectionMap.put(workerId, improved);
            workersCount++;
        }
        positiveAnswersCount += (improved ? 1 : 0);
        totalAnswersCount++;
    }

    public double getConsistency() {
        return (workersCount == 0 ? 1 : (consistentWorkersCount) / duplicateWorkersCount);
    }

    public double getTotalScore() {
        return totalAnswersCount == 0 ? -1 : positiveAnswersCount / totalAnswersCount;
    }

    public double getConsistentWorkersScore() {
        return consistentAnswersCount == 0 ? -1 : consistentPositiveAnswersCount / consistentAnswersCount;
    }

    @Override
    public int compareTo(Question another) {
        if (another.getTotalScore() < getTotalScore())
            return 1;
        if (another.getTotalScore() > getTotalScore())
            return -1;

        return 0;
    }

    public String getImageName() {
        return imageName;
    }

    public int getTotalUniqeWorkers() {
        return (int) workersCount;
    }

    public int getConsistantWorkers() {
        return (int) consistentWorkersCount;
    }

    public int getDuplicateWorkersCount() {
        return  duplicateWorkersCount;
    }
}
