package org.felix.thesis;

import java.util.ArrayList;
import java.util.List;

public class TestSetupResult {
    public TestSetupResult(String name, int port) {
        this.setupName = name;
        this.port = port;
    }

    String setupName;
    int port;

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
        sb.append("\n\u001b[m⎡‾‾‾‾‾‾‾‾[").append(this.setupName).append("][port=").append(this.port).append("]‾‾‾‾‾‾‾‾⎤");

        if (this.dockerBuildSuccessful) {
            sb.append("\n⎢ => build: successful");
        } else {
            sb.append("\u001b[0;31m"); //in red
            sb.append("\n⎢ => build: unsuccessful:");
            sb.append("\n⎢  -> stdOut: ").append(this.dockerBuildResult.stdOut);
            sb.append("\n⎢  -> stdErr: ").append(this.dockerBuildResult.stdErr);
            sb.append("\u001b[m"); //reset color
        }

        if (this.dockerRunSuccessful) {
            sb.append("\n⎢ => run: successful");
        } else {
            sb.append("\u001b[0;31m"); //in red
            sb.append("\n⎢ => run: unsuccessful:");
            sb.append("\n⎢  -> stdOut: ").append(this.dockerRunResult.stdOut);
            sb.append("\n⎢  -> stdErr: ").append(this.dockerRunResult.stdErr);
            sb.append("\u001b[m"); //reset color
        }

        if (this.allSuccessful) {
            sb.append("\u001b[0;32m"); //in green
            sb.append("\n⎢ => tests: successful");
            sb.append("\u001b[m"); //reset color
        } else {
            sb.append("\u001b[0;31m"); //in red
            sb.append("\n⎢ => tests: FAILED");
            sb.append("\u001b[m"); //reset color
        }
        sb.append("\n⎢ Subtests:");
        for (TestCaseResult caseRes : this.results) {
            sb.append("\n").append(caseRes.toStringShort());
        }
        sb.append("\n");
        //sb.append("\n⎣_______________________⎦");
        return sb.toString();
    }
}
