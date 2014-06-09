import java.io.*;
import java.util.*;

public class Main {

    static List<FullEntry> allEntries = new ArrayList<FullEntry>();
    static HashMap<String, Worker> workers = new HashMap<String, Worker>();
    static HashMap<String, Worker> badWorkers = new HashMap<String, Worker>();
    static HashMap<String, Question> questions = new HashMap<String, Question>();
    static List<String> blockedWorkers = new ArrayList<String>();
    static final double workerConsistencyThreshold = 0.70;
    static final double questionConsistencyThreshold = 0.75;
    static FullEntry header;
    static String startPath = null;
    static String endPath = null;

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide file path");
            System.exit(1);
        }

        String filePath = concatenate(args);
        File mainFile = new File(filePath);

        if (!mainFile.canWrite()) {
            System.out.println("File cannot be accessed");
            System.exit(2);
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\ofri\\Dropbox\\Phidicam R&D\\blocked_workers_list.txt"));
            String line = bufferedReader.readLine();

            while (line != null) {
                blockedWorkers.add(line);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(4);
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(mainFile));
            String line = bufferedReader.readLine();
            header = new FullEntry(line);
            FullEntry.setHeader(line);

            line = bufferedReader.readLine();
            while (line != null) {
                FullEntry fullEntry = new FullEntry(line);
                if (!blockedWorkers.contains(fullEntry.getWorkerId()))
                    allEntries.add(fullEntry);
                else
                    System.out.println("f");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(3);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(4);
        }

        // go over all entries and create workers list
        for (FullEntry entry : allEntries) {
            if (endPath == null) {
                endPath = entry.getEndPath();
                startPath = entry.getStartPath();
            }
            if (!workers.containsKey(entry.getWorkerId())) {
                workers.put(entry.getWorkerId(), new Worker());
            }
            workers.get(entry.getWorkerId()).addAnswer(entry.getImageName(), entry.selectedImprovedImage());
        }

        double totalConsistency = 0;
        double goodWorkersConsistency = 0;
        double badWorkersConsistency = 0;
        //filer out bad workers
        for (Map.Entry<String, Worker> worker : workers.entrySet()) {
            double consistency = worker.getValue().getConsistency();
            totalConsistency += consistency;
            if (consistency < workerConsistencyThreshold && worker.getValue().getNumberOfDuplicateQuestions() > 10) {
                badWorkers.put(worker.getKey(), worker.getValue());
                badWorkersConsistency += consistency;
            } else {
                goodWorkersConsistency += consistency;
            }
        }

        // approve good workers. reject bad ones
        for (FullEntry entry : allEntries) {
            entry.setApproved(!badWorkers.containsKey(entry.getWorkerId()));
        }

        for (FullEntry entry : allEntries) {
            if(entry.isApproved()) {
                if (!questions.containsKey(entry.getImageName())) {
                    questions.put(entry.getImageName(), new Question());
                }
                questions.get(entry.getImageName()).addAnswer(entry.getWorkerId(), entry.getImageName(),entry.selectedImprovedImage());
            }
        }

        // write back the full data file with the approvals and rejections
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(mainFile.getAbsolutePath().replace(".csv", "_up.csv")));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(header.toString()).append("\n");
            for (FullEntry entry :allEntries){
                stringBuilder.append(entry.toString()).append("\n");
            }
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(5);
        }


        // write new file to which summarises the results for each image couple
        List<Question> questionsTmp = new ArrayList<Question>(questions.values());
        Collections.sort(questionsTmp);

        int consistent = 0;
        double totalScore = 0;
        double consistentScore = 0;
        for (Question question : questionsTmp){
            totalScore += question.getTotalScore();
            if (question.getConsistency() >= questionConsistencyThreshold) {
                consistentScore += question.getConsistentWorkersScore();
                consistent++;
            }
        }

        consistentScore /= consistent;
        totalScore /= questionsTmp.size();

        HtmlReportMaker.setGlobalDetails(workers.size(),
                workers.size() - badWorkers.size(),
                (int)(workerConsistencyThreshold * 100),
                questions.size(),
                consistent,
                (int)(totalScore * 100),
                (int)(consistentScore * 100));

        try {
            String imageSummaryFileName = filePath.substring(0, filePath.lastIndexOf("."));
            String imageSummaryHtmlFileName = filePath.substring(0, filePath.lastIndexOf("."));
            imageSummaryFileName = imageSummaryFileName + "_summary.csv";
            imageSummaryHtmlFileName = imageSummaryHtmlFileName + "_summary.html";
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(imageSummaryFileName));
            BufferedWriter bufferedWriterHtml = new BufferedWriter(new FileWriter(imageSummaryHtmlFileName));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Star, end, total score, consistency, consistent score\n");
            for (Question question : questionsTmp){
                stringBuilder.append(startPath).append(question.getImageName()).append(",");
                stringBuilder.append(endPath).append(question.getImageName()).append(",");
                stringBuilder.append(question.getTotalScore()).append(",");
                stringBuilder.append(question.getConsistency()).append(",");
                stringBuilder.append(question.getConsistentWorkersScore());
                HtmlReportMaker.addLine(question, startPath, endPath);
            }
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriterHtml.write(HtmlReportMaker.getReport());
            bufferedWriter.close();
            bufferedWriterHtml.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(5);
        }

        generateBadWorkersFile(filePath, badWorkers);

        System.out.println("Statistics:");
        System.out.println("-------------");
        System.out.printf("Number of workers:               %d\n", workers.size());
        System.out.printf("Number of approved workers:      %d\n", workers.size() - badWorkers.size());
        System.out.printf("Number of rejected workers:      %d\n", badWorkers.size());
        System.out.println();
        System.out.printf("Total Consistency:               %.2f%s\n", totalConsistency / workers.size() * 100, "%");
        System.out.printf("Consistency threshold:           %.2f%s\n", workerConsistencyThreshold * 100, "%");
        System.out.printf("Good workers Consistency:        %.2f%s\n", goodWorkersConsistency / (workers.size() - badWorkers.size()) * 100, "%");
        System.out.printf("Bad workers Consistency:         %.2f%s\n", badWorkersConsistency / badWorkers.size() * 100, "%");

    }

    private static void generateBadWorkersFile(String filePath, HashMap<String, Worker> badWorkers) {
        try {
            String badWorkersSummery = filePath.substring(0, filePath.lastIndexOf("."));
            badWorkersSummery = badWorkersSummery + "_bad_workers.csv";
            BufferedWriter bufferedWriter= new BufferedWriter(new FileWriter(badWorkersSummery));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("worker_id, suspicious_answers\n");
            for (Map.Entry<String, Worker> worker : badWorkers.entrySet()){
                stringBuilder.append("\n");
                stringBuilder.append(worker.getKey()).append(":,");
                stringBuilder.append(worker.getValue().getConsistency()*100).append("%").append("\n");
                for (String question : worker.getValue().getInconsistentQuestions()) {
                    stringBuilder.append(worker.getKey()).append(",");
                    stringBuilder.append(question).append("\n");
                }

            }
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(5);
        }
    }

    private static String concatenate(String[] arr) {
        StringBuilder builder = new StringBuilder();
        for(String s : arr) {
            builder.append(s).append(" ");
        }
        String full = builder.toString();
        return full.substring(0, full.length()-1);
    }

}
