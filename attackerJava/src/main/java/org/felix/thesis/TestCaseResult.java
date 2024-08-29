package org.felix.thesis;

import java.util.List;

public class TestCaseResult {
    public TestCaseResult(String name) {
        this.testName = name;
    }
    String testName;
    List<DockerResult> commandResults;
    boolean passed;
    Exception exception;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("    TCR[");
        sb.append("\n\tname: ").append(this.testName);
        sb.append("\n\tpassed: ").append(this.passed);
        if (this.exception != null) {
            sb.append("\n\tException: ").append(this.exception);
        }
        sb.append("\n    ]");
        return sb.toString();
    }
}
