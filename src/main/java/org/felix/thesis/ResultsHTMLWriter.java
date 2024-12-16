package org.felix.thesis;

import java.io.FileNotFoundException;
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
        sb.append("<table>");

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
            sb.append("\n</rt>\n");
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

    private static String getPreText() {
        return """
<!DOCTYPE html>
<head>
<style>
table {
  font-family: arial, sans-serif;
  border-spacing: 0;
  max-width: 100vw; max-height: 100vh;
}
td, th {
  border: gray solid;
  border-width: 0 1px 1px 0;
  padding: .3rem;
  background: white;
}
th {
  border-bottom-width: 2px;
  position: sticky;
  top: 0;
  box-shadow: 0px 2px 2px -1px #0008;
}
th:first-child {
  z-index: 1;
}
th:first-child,
td:first-child {
  font-weight: bold;
  border-right-width: 2px;
  position: sticky;
  left: 0;
  box-shadow: 2px 0px 2px -1px #0008;
}
.unexpected {
  color: red;
  font-weight: bold;
}
.cheat {
  color: gray !important;
  font-style: italic;
}
tr:nth-child(2n) > td {
  background: #e8e8e8;
}
.open>td:not(:first-child) {
    color: gray;
}
</style>
</head>
<body>

""";
    }

    private static String getPostText() {
        return """
</body>
""";
    }
}
