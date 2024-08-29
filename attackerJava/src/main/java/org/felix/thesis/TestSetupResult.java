package org.felix.thesis;

import java.util.ArrayList;
import java.util.List;

public class TestSetupResult {
    public TestSetupResult(String name) {
        this.setupName = name;
    }

    String setupName;

    DockerResult dockerBuildResult;
    Exception dockerBuildException;
    Boolean dockerBuildSuccessful = true;

    DockerResult dockerRunResult;
    Exception dockerRunException;
    Boolean dockerRunSuccessful = true;

    List<TestCaseResult> results = new ArrayList<>();
    boolean allSuccessful = true;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TSR[");
        sb.append("\n\tName: ").append(this.setupName);
        if (this.dockerBuildSuccessful) {
            sb.append("\n\tbuild successful");
        } else {
            sb.append("\n\tbuild unsuccessful:");
            sb.append("\n\t stdOut: ").append(this.dockerBuildResult.stdOut);
            sb.append("\n\t stdErr: ").append(this.dockerBuildResult.stdErr);
        }
        if (this.dockerRunSuccessful) {
            sb.append("\n\trun successful");
        } else {
            sb.append("\n\trun unsuccessful:");
            sb.append("\n\t stdOut: ").append(this.dockerRunResult.stdOut);
            sb.append("\n\t stdErr: ").append(this.dockerRunResult.stdErr);
        }
        sb.append("\n\tTestSuccess: ").append(this.allSuccessful);
        sb.append("\n  Subtests:");
        for (TestCaseResult caseRes : this.results) {
            sb.append("\n").append(caseRes);
        }
        sb.append("\n]");
        return sb.toString();
    }
}
