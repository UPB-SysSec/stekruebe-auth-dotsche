package org.felix.thesis;

import javax.xml.transform.Result;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;

public class ResultsHTMLWriter {
    /**
     * creates a html file with the results nicely laid out in tables
     * @param res the list of TestSetupResults
     * @param path the path of the file to write to
     */
    public static void writeToFile(List<TestSetupResult> res, Path path) {
        StringBuilder sb = new StringBuilder();
        sb.append(ResultsHTMLWriter.getPreText());
        sb.append("<table id='table'>");

        //table:
        //  columns ~ tests
        //  rows ~ setups

        //print table headers
        sb.append("\n<tr>\n");
        sb.append("<th>Setup Name</th>");
        for (TestCaseResult caseRes : res.get(0).results) {
            sb.append("<th>").append(caseRes.testName).append("</th>");
        }
        sb.append("\n</tr>\n");

        //print setup results in rows
        for (TestSetupResult testRes : res) {
            sb.append("\n<tr");
            if (testRes.setupName.endsWith("_open")) { //add ".open" class if setup is open
                sb.append(" class=\"open\"");
            }
            sb.append(">\n"); // one setup per row
            sb.append("<td>").append(testRes.setupName).append("</td>"); //name row
            for (TestCaseResult caseRes : testRes.results) {
                sb.append("<td class=\"");
                if (!caseRes.wasOutcomeExpected()) { //add ".unexpected" class if outcome is not normal
                    sb.append("unexpected");
                }
                if (caseRes.testName.startsWith("[D]")) {
                    sb.append(" cheat");
                }
                sb.append("\">");
                sb.append(caseRes.testOutcome).append("</td>");
            }
            sb.append("\n</tr>\n");
        }

        sb.append("</table>");
        sb.append(ResultsHTMLWriter.getPostText());
        try {
            PrintWriter out = new PrintWriter(path.toString());
            out.print(sb); // write to file
            out.close();
        } catch (FileNotFoundException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getTemplate(){
        var is = ResultsHTMLWriter.class.getClassLoader().getResourceAsStream("output_template.html");
        try {
            return new String(is.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final String SPLITTER = "<body>";

    private static String getPreText() {
        String template = getTemplate();
        return template.substring(0, template.indexOf(SPLITTER)+SPLITTER.length());
    }

    private static String getPostText() {
        String template = getTemplate();
        return template.substring(template.indexOf(SPLITTER)+SPLITTER.length());
    }
}
