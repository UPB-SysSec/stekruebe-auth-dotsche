package org.felix.thesis;

public class DockerResult {
    public String stdOut;
    public String stdErr;
    public int exitVal;
    public Exception exception;
    public String debugMessage;

    DockerResult() {}
    DockerResult(String stdOut, String stdErr, int exitVal) {
        this.stdOut = stdOut;
        this.stdErr = stdErr;
        this.exitVal = exitVal;
    }
    DockerResult(Exception error, String debugMessage) {
        this.debugMessage = debugMessage;
        this.exception = error;
        this.exitVal = -1;
    }
    DockerResult(DockerResult twin) {
        this.exception = twin.exception;
        this.exitVal = twin.exitVal;
        this.stdOut = twin.stdOut;
        this.stdErr = twin.stdErr;
        this.debugMessage = twin.debugMessage;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DockerResult[");
        sb.append(this.exitVal);
        sb.append("](");
        if (this.exception != null) {
            sb.append("Exception=");
            sb.append(this.exception.toString());
        } else {
            sb.append("  stdOut=");
            sb.append(this.stdOut);
            sb.append("  stdErr=");
            sb.append(this.stdErr);
        }
        if (this.debugMessage!=null) {
            sb.append(" MSG:");
            sb.append(this.debugMessage);
        }
        sb.append(")");
        return sb.toString();
    }
}
