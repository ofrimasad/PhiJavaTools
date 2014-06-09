import java.util.ArrayList;

public class FullEntry {

    private static int sizeOfHeader;
    private String[] values;
    private static int indexOfAnswer = 0;
    private static int indexOfOptionA = 0;
    private static int indexOfOptionB = 0;
    private static int indexOfWorkerId = 0;
    private static int indexOfApproved = 0;
    private static int indexOfReject = 0;

    private boolean approved = false;

    public static void setHeader(String header) {
        String[] titles = header.split(",");

        for (int i = 0; i < titles.length; i++) {
            if ("\"Input.A\"".equals(titles[i]))
                indexOfOptionA = i;
            if ("\"Input.B\"".equals(titles[i]))
                indexOfOptionB = i;
            if ("\"Answer.flagged\"".equals(titles[i]))
                indexOfAnswer = i;
            if ("\"WorkerId\"".equals(titles[i]))
                indexOfWorkerId = i;
            if ("\"Approve\"".equals(titles[i]))
                indexOfApproved = i;
            if ("\"Reject\"".equals(titles[i]))
                indexOfReject = i;
        }
        sizeOfHeader = titles.length;

        if (indexOfAnswer == 0
                || indexOfOptionB == 0
                || indexOfOptionA == 0
                || indexOfWorkerId == 0
                || indexOfApproved == 0
                || indexOfReject == 0) {
            throw new RuntimeException("header syntax miss-match");
        }

    }

    public FullEntry(String line) {

        String[] tmpArray = line.split(",");
        ArrayList<String> tmpList = new ArrayList<String>();
        StringBuilder stringBuilder = null;
        for (String s : tmpArray) {
            if (!s.endsWith("\"") && s.startsWith("\"")) {
                stringBuilder = new StringBuilder(s).append(",");
            } else if (!s.startsWith("\"") && s.endsWith("\"")) {
                stringBuilder.append(s);
                tmpList.add(stringBuilder.toString());
                stringBuilder = null;
            } else if (stringBuilder != null) {
                stringBuilder.append(s).append(",");
            } else {
                tmpList.add(s.replaceAll("\"", ""));
            }
        }
        if (tmpList.size() < sizeOfHeader) {
            int toAdd = sizeOfHeader - tmpList.size();
            for (int i = 0; i < toAdd; i++)
                tmpList.add("");
        }
        values = new String[tmpList.size()];
        tmpList.toArray(values);
    }

    public String getWorkerId() {
        return values[indexOfWorkerId];
    }

    public void setApproved(boolean approved) {
        if (approved) {
            values[indexOfApproved] = "x";
            values[indexOfReject] = "";
        } else {
            values[indexOfApproved] = "";
            values[indexOfReject] = "Results were inconsistent (over 30%)";
        }
        this.approved = approved;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getImageName() {
        String fullName = values[indexOfOptionA].split("/")[values[indexOfOptionA].split("/").length-1];
        return fullName.substring(2);
    }

    public boolean selectedImprovedImage() {
        String answer = values[indexOfAnswer];
        String selection = values[answer.equals("A") ? indexOfOptionA : indexOfOptionB];
        return selection.substring(selection.lastIndexOf("/") + 1).startsWith("e_");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(String s : values) {
            if (s.contains(",")) {
                builder.append(s).append(",");
            } else {
                builder.append(s).append(",");
            }
        }
        String full = builder.toString();
        return full.substring(0, full.length()-1);
    }

    public String getEndPath() {
        return getPath() + "/end/e_";
    }

    public String getStartPath() {
        return getPath() + "/start/s_";
    }

    private String getPath() {
        String path = values[indexOfOptionA].substring(0, values[indexOfOptionA].lastIndexOf("/"));
        return path.substring(0, path.lastIndexOf("/"));
    }
}

