import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Worker {

    private HashMap<String, Boolean> ImageNameToSelectionMap = new HashMap<String, Boolean>();
    private double duplicateQuestionCount = 0;
    private double consistentDuplicateQuestionCount = 0;
    private List<String> consistentQuestions = new ArrayList<String>();
    private List<String> inconsistentQuestions = new ArrayList<String>();

    public void addAnswer(String imageName, boolean improved) {
        if (ImageNameToSelectionMap.containsKey(imageName)) {
            duplicateQuestionCount++;
            if ((improved && ImageNameToSelectionMap.get(imageName) || (!improved && !ImageNameToSelectionMap.get(imageName)))) {
                // consistent
                consistentDuplicateQuestionCount++;
                consistentQuestions.add(imageName);
            } else {
                inconsistentQuestions.add(imageName);
            }
            ImageNameToSelectionMap.remove(imageName);
        } else {
            ImageNameToSelectionMap.put(imageName, improved);
        }
    }

    public double getConsistency() {
        return (duplicateQuestionCount == 0 ? 1 : consistentDuplicateQuestionCount / duplicateQuestionCount);
    }

    public int getNumberOfDuplicateQuestions() {
        return (int) duplicateQuestionCount;
    }

    public boolean isConsistentQuestion(String imageName) {
        return this.consistentQuestions.contains(imageName) || ImageNameToSelectionMap.containsKey(imageName);
    }

    public List<String> getInconsistentQuestions() {
        return inconsistentQuestions;
    }
}
