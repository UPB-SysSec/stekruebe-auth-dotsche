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
  z-index: 2;
  top: 0;
  box-shadow: 0px 2px 2px -1px #0008;
}
th:first-child,
td:first-child {
  font-weight: bold;
  border-right-width: 2px;
  position: sticky;
  left: 0;
  z-index: 2;
  box-shadow: 2px 0px 2px -1px #0008;
}
th:first-child {
  z-index: 3;
}
td:not(:first-child) {
  position: relative;
}
/* vertical line after 2 cols */
td:nth-child(2n+1) {
  border-right-width: 2px;
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
/* to have a line after x, x_defaultB, x_defaultC */
tr:nth-child(3n+1) > td {
  border-bottom: 4px solid black;
}
.open>td:not(:first-child) {
    color: gray;
}
.flag {
  --w: 24px;
  opacity: .3;
  width: var(--w);
  height: 100%;
  position: absolute;
  right: 0;
  top: 0;
}
.flag:nth-last-of-type(2) {
  right: calc(1 * var(--w))
}
.flag:nth-last-of-type(3) {
  right: calc(2 * var(--w))
}
.flag:nth-last-of-type(4) {
  right: calc(3 * var(--w))
}
.flag:hover {
  outline: #000a 3px solid;
  outline-offset: -3px;
}
</style>
<script>
var d = {};

document.addEventListener("DOMContentLoaded", function() {
  d.table = document.getElementById('table');
  d.rows = Array.from(table.querySelectorAll('tbody tr'));
  d.header = d.rows.shift()
  d.body = d.table.querySelectorAll('tbody')[0]
  sortTable()
  //highlightContent();
  highlightSubDomainSiblings();
  highlightBCSiblings();
  highlightCertASiblings();
});

function getFirstTD(td) {
  return getTR(td).children[0]
}
function getTR(td) {
  return td.parentElement
}
function getTH(td) {
  var index = Array.prototype.indexOf.call(td.parentNode.children, td)
  return table.querySelector('th:nth-child(' + (index+1) + ')')
}
function getTD(tr, th) {
  var index = Array.prototype.indexOf.call(th.parentNode.children, th)
  return tr.children[index]
}

function strGetTR(td_s) { //get tr from string
  for (let tr of d.rows) {
    if (tr.children[0].innerText==td_s) {
      return tr
    }
  }
}
function domToSub(tr) { //get _subdomains row from _domains row
  var subName = tr.children[0].textContent.replace('domains', 'subdomains')
  return strGetTR(subName)
}
function getDefBC(tr) { //get _defaultb and _d rows from _defaulta row
  var name = tr.children[0].textContent
  var r = []
  r.push(strGetTR(name.replace("_defaulta", "_defaultb")))
  r.push(strGetTR(name.replace("_defaulta", "")))
  r = r.filter(n => n)
  return r
}
function addCertA(tr) { //get _certa row from normal row
  var certAName = tr.children[0].textContent.replace('domains', 'domains_certa')
  return strGetTR(certAName)
}

function sortTable() {
  function comparer(a, b) {return getFirstTD(a).innerText < getFirstTD(b).innerText}
  d.rows.sort(comparer).forEach(tr => d.table.appendChild(tr) );
}

function highlightContent() { //highlight all cells where _domain != _subdomain
  for (let row of d.rows) {
    for (let cell of row.children) {
      if (cell==row.children[0]) continue //skip header
      if (!cell.classList.contains("unexpected")) continue //skip where expected
      if (cell.textContent.includes("contentA")) {
        addFlag(cell, "#af0", "gives contentA when we didn't ask")
      }
      else if (cell.textContent.includes("contentB")) {
        addFlag(cell, "#f00", "leaks contentB!")
      }
      else if (cell.textContent.includes("contentC")) {
        addFlag(cell, "#af0", "gives contentC when we didn't ask")
      }
    }
  }
}
function highlightSubDomainSiblings() { //highlight all cells where _domain != _subdomain
  for (let row of d.rows) {
    if (!row.children[0].textContent.includes("_domains_")) {continue}

    const subTR = domToSub(row)

    for (let cell of row.children) {
      if (cell==row.children[0]) continue //skip header
      const th = getTH(cell)
      const sibling = getTD(subTR, th)
      if (cell.textContent != sibling.textContent) {
        addFlag(cell, "#fa0", "different than in _subdomains_")
        addFlag(sibling, "#fa0", "different than in _domains_")
      }
    }
  }
}
function highlightBCSiblings() { //highlight all cells where standard != _defB != _defC
  for (let row of d.rows) {
    if (!row.children[0].textContent.includes("_defaulta")) {continue}
    const bc = getDefBC(row)

    for (let cell of row.children) {
      if (cell==row.children[0]) continue //skip header
      const th = getTH(cell)
      var same = true
      for (let s of bc) {
        if (getTD(s, th).textContent != cell.textContent) {
          same = false
          break
        }
      }
      if (!same) {
        addFlag(cell,"#0af", "different than in other _defaults")
        for (let s of bc) {
          addFlag(getTD(s, th), "#0af", "different than in other _defaults")
        }
      }
    }
  }
}
function highlightCertASiblings() { //highlight all cells where _domain != _subdomain
  for (let row of d.rows) {
    if (row.children[0].textContent.includes("_certa")) {continue}

    const certATR = addCertA(row)

    for (let cell of row.children) {
      if (cell==row.children[0]) continue //skip header
      const th = getTH(cell)
      const sibling = getTD(certATR, th)
      if (cell.textContent != sibling.textContent) {
        addFlag(cell, "#c0c", "different than in _certA")
        addFlag(sibling, "#c0c", "different than in NON _certA")
      }
    }
  }
}

function addFlag(td, color, title) { //adds a colored flag to the element
  const flag = document.createElement("div")
  flag.style.background = color
  flag.classList.add("flag")
  flag.setAttribute("title", title)
  td.appendChild(flag)
}
</script>
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
