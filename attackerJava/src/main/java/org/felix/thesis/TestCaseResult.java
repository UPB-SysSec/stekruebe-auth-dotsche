package org.felix.thesis;

import de.rub.nds.tlsattacker.core.constants.AlertDescription;
import de.rub.nds.tlsattacker.core.constants.AlertLevel;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;

import java.nio.charset.StandardCharsets;

public class TestCaseResult {
    public TestCaseResult(String name, boolean expectedToFail) {
        this.testName = name;
        this.expectedToFail = expectedToFail; //invert
    }

    String testName;
    boolean receivedDataFromB = false;
    boolean expectedToFail;

    Boolean requestAWorkflowExecutedAsPlanned = false;
    Boolean requestAHadTicket = true;
    Throwable requestAException;
    AlertMessage requestAAlert;
    WorkflowTrace requestATrace;

    int requestBStatusCode = 0;
    Boolean requestBWorkflowExecutedAsPlanned = false;
    int requestBHttpStatusCode = -1;
    Throwable requestBException;
    AlertMessage requestBAlert;
    ApplicationMessage requestBApplicationData;
    WorkflowTrace requestBTrace;

    public String toStringShort() {
        StringBuilder sb = new StringBuilder();
        sb.append("⎡Test Result: ").append(this.testName);


        // request A
        sb.append("\n⎢ ⎡Request A resulted in ");
        if (requestAHadTicket) {
            sb.append("a Ticket");
        } else if (this.requestAAlert != null) {
            sb.append("a TLS alert (")
                    .append(AlertLevel.getAlertLevel((Byte) this.requestAAlert.getLevel().getValue()).name())
                    .append("): ")
                    .append(AlertDescription.getAlertDescription((Byte) this.requestAAlert.getDescription().getValue()).name());
            //sb.append("\n").append(this.requestATrace);
        } else if (this.requestAException != null) {
            sb.append("an exception: ").append(this.requestBException);
        } else {
            sb.append("something unexpected");
        }

        // request B
        sb.append("\n⎢ ⎣Request B resulted in ");
        if (this.requestBHttpStatusCode != -1) {
            sb.append("the HTTP code ").append(this.requestBHttpStatusCode);
        } else if (this.requestBAlert != null) {
            sb.append("a TLS alert [")
                    .append(AlertLevel.getAlertLevel((Byte) this.requestBAlert.getLevel().getValue()).name())
                    .append("]: ")
                    .append(AlertDescription.getAlertDescription((Byte) this.requestBAlert.getDescription().getValue()).name());
        } else if (this.requestBException != null) {
            sb.append("an exception: ").append(this.requestBException);
        } else if (!this.requestAHadTicket) {
            sb.append("nothing (wasn't sent because A had no ticket)");
        } else if (this.requestBApplicationData != null) {
            sb.append("application data, that didn't contain a http status code");
        } else if (this.requestBWorkflowExecutedAsPlanned) {
            sb.append("an empty response, while the workflow was executed as planned");
        } else {
            sb.append("something unexpected: received no alert or application data, threw no exception ...");
        }

        // wrap up
        boolean expectedThisResult = (this.requestBHttpStatusCode==200) != this.expectedToFail;
        if (!expectedThisResult) sb.append("\u001b[0;31m"); //in red
        sb.append("\n⎣was that expected?:      ").append(expectedThisResult);

        sb.append("\u001b[m"); //reset color
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("⎡Test Result: ").append(this.testName);
        sb.append("\n⎢ ⎡Request A:");
        if (this.requestAHadTicket) {
            sb.append("\n⎢ ⎢ the request resulted in a ticket");
        } else {
            sb.append("\u001b[0;31m"); //in red
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
        if (this.requestAWorkflowExecutedAsPlanned) {
            sb.append("\n⎢ ⎣ => request A workflow was executed as planned");
        } else {
            sb.append("\n⎢ ⎣ => request A encountered an issue with the workflow");
        }
        if (!this.requestAHadTicket) sb.append("\u001b[m"); //reset color



        if (this.requestAHadTicket) {
            if (this.requestBHttpStatusCode==-1) {
                sb.append("\u001b[0;33m"); //in yellow
            } else if (this.requestBHttpStatusCode==200) {
                sb.append("\u001b[0;32m"); //in green
            } else {
                sb.append("\u001b[0;96m"); //in bright cyan
            }
            sb.append("\n⎢ ⎡Request B:");
            if (this.requestBHttpStatusCode!=-1) {
                sb.append("\n⎢ ⎢ => request B got the status code ")
                        .append(this.requestBHttpStatusCode);
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
            if (this.requestBApplicationData != null) {
                String content = new String(this.requestBApplicationData.getData().getOriginalValue(), StandardCharsets.UTF_8);
                sb.append("\n⎢ ⎢ Response Content [");
                sb.append(content.replace("\r\n", "\\n").substring(0, Math.min(75, content.length())))
                        .append("]");
            }
            if (this.requestBWorkflowExecutedAsPlanned) {
                sb.append("\n⎢ ⎣ => request B workflow was executed as planned");
            } else {
                sb.append("\n⎢ ⎣ => request B encountered an issue with the workflow");
            }
            if (!this.requestBWorkflowExecutedAsPlanned) sb.append("\u001b[m"); //reset color
        }
        sb.append("\n⎢did the server complain: ").append(!this.receivedDataFromB);
        boolean expectedThisResult = this.receivedDataFromB != this.expectedToFail;
        if (!expectedThisResult) sb.append("\u001b[0;31m"); //in red
        sb.append("\n⎣was that expected?:      ").append(expectedThisResult);
        sb.append("\u001b[m"); //reset color
        return sb.toString();
    }
}
