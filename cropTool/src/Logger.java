
import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Logger {
    private static final String divider = ",";


    private static final String TAG = "Logger";
    private List<LogEntry> entries = new ArrayList<LogEntry>();

    public HashMap<String, LogEntry> readFile(String fileName) {
        HashMap<String, LogEntry> logEntries = new HashMap<String, LogEntry>();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(fileName));
            String nextLine = bufferedReader.readLine();
            String[] fields = nextLine.split(divider);
            nextLine = bufferedReader.readLine();

            while (nextLine != null) {
                LogEntry logEntry = new LogEntry();
                logEntry.setFields(fields, nextLine.split(divider));
                logEntries.put(logEntry.fileName, logEntry);
                nextLine = bufferedReader.readLine();
            }
        } catch (FileNotFoundException e) {

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return logEntries;
    }

    public void addEntry(LogEntry entry) {
        entries.add(entry);
    }

    public void writeToFile(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));

            //write the titles
//            if (!entries.isEmpty()) {
//                bufferedWriter.write(entries.get(0).getFields());
//                bufferedWriter.newLine();
//            }

            for (LogEntry logEntry : entries) {
                bufferedWriter.append(logEntry.toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class LogEntry {

        public String fileName;
//        public int width;
//        public int height;

        public int startTop;
        public int startLeft;
        public int startRight;
        public int startBottom;

//        public int endLeft;
//        public int endRight;
//        public int endTop;
//        public int endBottom;

//        public int startRank;
//        public int endRank;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (Field f : LogEntry.class.getFields()) {
                try {
                    sb.append(f.get(this).toString()).append(divider);
                } catch (IllegalAccessException ignored) {
                }
            }
            return sb.toString().substring(0, sb.toString().length()-1);
        }

        public String getFields() {
            StringBuilder sb = new StringBuilder();
            for (Field f : LogEntry.class.getFields()) {
                sb.append(f.getName()).append(divider);
            }
            return sb.toString();
        }

        public void setFields(String[] fieldsNames, String[] fieldsValues) {
            for (int i = 0; i < fieldsNames.length; i++) {
                Object val = fieldsValues[i];
                try {
                    val = Integer.parseInt(val.toString());
                } catch (NumberFormatException e) {
                    try {
                        val = Double.parseDouble(val.toString());
                    } catch (NumberFormatException e1) {
                        e.printStackTrace();
                    }
                }
                try {
                    Field field = LogEntry.class.getField(fieldsNames[i]);
                    field.set(this, val);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
