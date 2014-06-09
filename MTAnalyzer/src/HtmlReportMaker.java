
public class HtmlReportMaker {

    private static int counter = 1;
    private static final String head = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"utf-8\">\n" +
            "    <meta name=\"generator\" content=\"CoffeeCup HTML Editor (www.coffeecup.com)\">\n" +
            "    <meta name=\"dcterms.created\" content=\"Sun, 09 Mar 2014 09:24:55 GMT\">\n" +
            "    <meta name=\"description\" content=\"\">\n" +
            "    <meta name=\"keywords\" content=\"\">\n" +
            "    <title>Mechanical Turk Report</title>\n" +
            "    \n" +
            "    <style type=\"text/css\">\n" +
            "    <!--\n" +
            "    body {\n" +
            "      color:#000000;\n" +
            "      background-color:#FFFFFF;\n" +
            "    }\n" +
            "    a  { color:#0000FF; }\n" +
            "    a:visited { color:#800080; }\n" +
            "    a:hover { color:#008000; }\n" +
            "    a:active { color:#FF0000; }\n" +
            "    -->\n" +
            "    </style>\n" +
            "    <!--[if IE]>\n" +
            "    <script src=\"http://html5shim.googlecode.com/svn/trunk/html5.js\"></script>\n" +
            "    <![endif]-->\n" +
            "  </head>\n" +
            "  <body>\n" +
            "   Total number of workers: %d</br>" +
            "   Number of approved workers: %d</br>" +
            "   Threshold for rejecting workers: %d%s</br>" +
            "   </br>" +
            "   Total number of Questions: %d</br>" +
            "   Number of Consistent Questions: %d</br>" +
            "</br>" +
            "   Total Score: %d%s</br>" +
            "   Score of Consistent Questions %d%s</br>" +
            "</br></br>" +
            "<table border=\"1\"  width=\"100%s\">\n" +
            "<col width=\"350\">\n" +
            "<col width=\"350\">\n" +
            "  <col width=\"250\">";
    private static final String row = "  <tr>\n" +
            "     <td>%s</td>\n" +
            "  </tr>\n" +
            "  <tr>\n" +
            "     <td><img src=\"%s\" width=\"320\" /></td>\n" +
            "     <td><img src=\"%s\" width=\"320\" /></td>\n" +
            "     <td>#%d</br>Total Score: %d%s</br>Consistency: %d%s</br>Consistent Score: %d%s</br>Number of workers: %d</br>Number of Consistent workers: %d of %d</td>\n" +
            "     <td></td>" +
            "  </tr>";
    private static final String tail = "</table>\n" +
            "\n" +
            "  </body>\n" +
            "</html>";

    private static StringBuilder stringBuilder = new StringBuilder();

    public static void setGlobalDetails(int totalNumOfWorkers,
                                        int numOfApprovedWorkers,
                                        int workersThreshold,
                                        int numberOfQuestions,
                                        int numberOfConsistentQuestions,
                                        int totalScore,
                                        int consistentScore) {
        stringBuilder.append(String.format(head,
                totalNumOfWorkers,
                numOfApprovedWorkers,
                workersThreshold, "%",
                numberOfQuestions,
                numberOfConsistentQuestions,
                totalScore, "%",
                consistentScore, "%", "%"));
    }

    public static String getReport() {
        stringBuilder.append(tail);
        return stringBuilder.toString();
    }

    public static void addLine(Question question, String startUrl, String endUrl) {
        stringBuilder.append(String.format(
                row,
                question.getImageName(),
                startUrl + question.getImageName(),
                endUrl + question.getImageName(),
                counter++,
                (int)(question.getTotalScore() * 100),
                "%",
                (int)(question.getConsistency() * 100),
                "%",
                (int)(question.getConsistentWorkersScore() * 100),
                "%",
                question.getTotalUniqeWorkers(),
                question.getConsistantWorkers(),
                question.getDuplicateWorkersCount()));
    }
}
