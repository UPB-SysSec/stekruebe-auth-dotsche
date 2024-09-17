package org.felix.thesis;

import de.rub.nds.tlsattacker.core.constants.AlertDescription;
import de.rub.nds.tlsattacker.core.constants.AlertLevel;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;

public class TestCaseResult {
    public TestCaseResult(String name, boolean expectedToFail) {
        this.testName = name;
        this.expectedToFail = expectedToFail; //invert
    }

    String testName;
    boolean ranWithoutIssue = true;
    boolean expectedToFail;

    int requestAStatusCode = 0;
    Boolean requestAExecutedWithoutIssue = false;
    Boolean requestAHadTicket = true;
    Throwable requestAException;
    AlertMessage requestAAlert;
    WorkflowTrace requestATrace;

    int requestBStatusCode = 0;
    Boolean requestBExecutedWithoutIssue = false;
    Throwable requestBException;
    AlertMessage requestBAlert;
    WorkflowTrace requestBTrace;

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("⎡Test Result: ").append(this.testName);
        sb.append("\n⎢ ⎡Request A:");
        if (this.requestAStatusCode != 0) {
            sb.append("\n⎢ ⎢ status Code: ").append(requestAStatusCode);
        }
        if (this.requestAHadTicket) {
            sb.append("\n⎢ ⎢ the request resulted in a ticket");
        } else {
            sb.append("\u001b[0;33m"); //in yellow
            sb.append("\n⎢ ⎢ the request did NOT result in a ticket");
            if (this.requestAException != null) {
                sb.append("\n⎢ ⎢Exception: ").append(this.requestAException);
            }
        }
        if (this.requestAAlert != null) {
            sb.append("\n⎢ ⎢ TLS alert[")
                    .append(AlertLevel.getAlertLevel((Byte) this.requestAAlert.getLevel().getValue()).name())
                    .append("]: ")
                    .append(AlertDescription.getAlertDescription((Byte) this.requestAAlert.getDescription().getValue()).name());
        }
        if (this.requestAExecutedWithoutIssue) {
            sb.append("\n⎢ ⎣ => request A was executed without issue");
        } else {
            sb.append("\n⎢ ⎣ => request A encountered an issue");
        }
        if (!this.requestAHadTicket) sb.append("\u001b[m"); //reset color

        if (this.requestAHadTicket) {
            if (!this.requestBExecutedWithoutIssue) sb.append("\u001b[0;33m"); //in yellow
            sb.append("\n⎢ ⎡Request B:");
            if (this.requestBStatusCode != 0) {
                sb.append("\n⎢ ⎢ status Code: ").append(requestBStatusCode);
            }
            if (this.requestBException != null) {
                sb.append("\n⎢ ⎢ Exception: ").append(this.requestBException);
            }
            if (this.requestBAlert != null) {
                sb.append("\n⎢ ⎢ TLS alert[")
                        .append(AlertLevel.getAlertLevel((Byte) this.requestBAlert.getLevel().getValue()).name())
                        .append("]: ")
                        .append(AlertDescription.getAlertDescription((Byte) this.requestBAlert.getDescription().getValue()).name());
            }
            if (this.requestBExecutedWithoutIssue) {
                sb.append("\n⎢ ⎣ => request B was executed without issue");
            } else {
                sb.append("\n⎢ ⎣ => request B encountered an issue");
            }
            if (!this.requestBExecutedWithoutIssue) sb.append("\u001b[m"); //reset color
        }
        sb.append("\n⎢did the server complain: ").append(!this.ranWithoutIssue);
        boolean expectedThisResult = this.ranWithoutIssue != this.expectedToFail;
        if (!expectedThisResult) sb.append("\u001b[0;31m"); //in red
        sb.append("\n⎣was that expected?:      ").append(expectedThisResult);
        sb.append("\u001b[m"); //reset color
        return sb.toString();
    }
}
