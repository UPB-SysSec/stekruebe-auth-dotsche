package org.felix.thesis;

import de.rub.nds.tlsattacker.core.constants.AlertDescription;
import de.rub.nds.tlsattacker.core.exceptions.TransportHandlerConnectException;
import de.rub.nds.tlsattacker.core.protocol.ProtocolMessage;
import de.rub.nds.tlsattacker.core.protocol.message.AlertMessage;
import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTraceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class TestCaseResult {
    private final Logger LOGGER;

    public TestCaseResult(String name) {
        this.testName = name;
        LOGGER = LogManager.getLogger(name);
    }

    String testName;
    TestOutcome[] expectedTestOutcome;

    WorkflowTrace requestATrace;
    Throwable requestAException;

    boolean executedRequestB = false;
    WorkflowTrace requestBTrace;
    Throwable requestBException;

    int httpCodeA = -1;
    String siteContentA;
    int httpCodeB = -1;
    String siteContentB;

    TestOutcome testOutcome;

    public boolean wasOutcomeExpected() {
        return Arrays.stream(expectedTestOutcome).anyMatch((x)->x==this.getTestOutcome());
    }

    public TestOutcome getTestOutcome() {
        if(this.testOutcome == null) {
            this.testOutcome = this.getTestOutcome_int();
        }
        return this.testOutcome;
    }
    private TestOutcome getTestOutcome_int() {
        if (!executedRequestB) { //outcome with A
            if (requestAException != null) {
                if (requestAException instanceof ConnectException) {
                    return TestOutcome.firstRequest_exception_noConnection;
                } else if (requestAException instanceof TransportHandlerConnectException) {
                    return TestOutcome.firstRequest_exception_noConnection;
                } else {
                    return TestOutcome.firstRequest_exception_other;
                }
            }

            ApplicationMessage appMessage = requestATrace.getFirstReceivedMessage(ApplicationMessage.class);
            if (appMessage!=null) {
                siteContentA = getFullContent(requestATrace);
                int httpCode = getHTTPCode(siteContentA);
                if (httpCode != -1) {
                    httpCodeA = httpCode;
                    return switch (httpCode) {
                        case 400 -> TestOutcome.firstRequest_http400_badRequest;
                        case 403 -> TestOutcome.firstRequest_http403_forbidden;
                        case 404 -> TestOutcome.firstRequest_http404_notFound;
                        case 421 -> TestOutcome.firstRequest_http421_misdirectedRequest;
                        default  -> TestOutcome.firstRequest_httpOther;
                    };
                } else {
                    return TestOutcome.firstRequest_applicationDataButNoHTTPStatusCode;
                }
            }

            AlertMessage alertMessage = requestATrace.getFirstReceivedMessage(AlertMessage.class);
            if (alertMessage!=null) {
                AlertDescription alert = AlertDescription.getAlertDescription((Byte) alertMessage.getDescription().getValue());
                return switch (alert) {
                    case UNEXPECTED_MESSAGE -> TestOutcome.firstRequest_tlsAlert_unexpectedMessage;
                    case INTERNAL_ERROR     -> TestOutcome.firstRequest_tlsAlert_internalError;
                    case UNKNOWN_CA         -> TestOutcome.firstRequest_tlsAlert_unknownCA;
                    case UNRECOGNIZED_NAME  -> TestOutcome.firstRequest_tlsAlert_unknownName;
                    case ACCESS_DENIED      -> TestOutcome.firstRequest_tlsAlert_accessDenied;
                    default                 -> TestOutcome.firstRequest_tlsAlert_other;
                };
            }

            return TestOutcome.firstRequest_noApplicationData;
        } else { //outcome with B
            if (requestBException != null) {
                if (requestBException instanceof ConnectException) {
                    return TestOutcome.secondRequest_exception_noConnection;
                } else if (requestBException instanceof TransportHandlerConnectException) {
                    return TestOutcome.secondRequest_exception_noConnection;
                } else {
                    return TestOutcome.secondRequest_exception_other;
                }
            }

            ApplicationMessage appMessage = requestBTrace.getFirstReceivedMessage(ApplicationMessage.class);
            if (appMessage!=null) {
                siteContentB = getFullContent(requestBTrace);
                int httpCode = getHTTPCode(siteContentB);
                if (httpCode != -1) {
                    httpCodeB = httpCode;
                    if (httpCode==200) {
                        if (siteContentB.toLowerCase().contains("site a") || siteContentB.toLowerCase().contains("sitea")) {
                            return TestOutcome.secondRequest_http200_contentA;
                        } else if (siteContentB.toLowerCase().contains("site b") ||  siteContentB.toLowerCase().contains("siteb")) {
                            return TestOutcome.secondRequest_http200_contentB;
                        } else {
                            //what
                            return TestOutcome.secondRequest_http200_unknownContent;
                        }
                    }
                    return switch (httpCode) {
                        case 400 -> TestOutcome.secondRequest_http400_badRequest;
                        case 403 -> TestOutcome.secondRequest_http403_forbidden;
                        case 404 -> TestOutcome.secondRequest_http404_notFound;
                        case 421 -> TestOutcome.secondRequest_http421_misdirectedRequest;
                        default  -> TestOutcome.secondRequest_httpOther;
                    };
                } else {
                    return TestOutcome.secondRequest_applicationDataButNoHTTPStatusCode;
                }
            }

            AlertMessage alertMessage = requestBTrace.getFirstReceivedMessage(AlertMessage.class);
            if (alertMessage!=null) {
                AlertDescription alert =  AlertDescription.getAlertDescription((Byte) alertMessage.getDescription().getValue());
                return switch (alert) {
                    case UNEXPECTED_MESSAGE -> TestOutcome.secondRequest_tlsAlert_unexpectedMessage;
                    case INTERNAL_ERROR     -> TestOutcome.secondRequest_tlsAlert_internalError;
                    case UNKNOWN_CA         -> TestOutcome.secondRequest_tlsAlert_unknownCA;
                    case UNRECOGNIZED_NAME  -> TestOutcome.secondRequest_tlsAlert_unknownName;
                    case ACCESS_DENIED      -> TestOutcome.secondRequest_tlsAlert_accessDenied;
                    default                 -> TestOutcome.secondRequest_tlsAlert_other;
                };
            }

            return TestOutcome.secondRequest_noApplicationData;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        TestOutcome testOutcome = getTestOutcome();
        if (wasOutcomeExpected()) { //as expected
            sb.append("\u001b[0;32m"); //in green
            sb.append(  "⎢   ⎡Test Result: ").append(this.testName);
            sb.append("\n⎢   ⎣got       [").append(testOutcome).append(        "] as expected");
        } else { //unexpected
            sb.append("\u001b[0;31m"); //in red
            sb.append(  "⎢   ⎡Test Result: ").append(this.testName);
            if (!executedRequestB) {
                if (siteContentA != null) {
                    String simpleBody = getBodySimple(siteContentA);
                    sb.append("\n⎢   ⎢BodyA: ").append(simpleBody, 0, Math.min(simpleBody.length(), 300));
                }
//                if (httpCodeA!=-1) {
//                    sb.append("\n⎢   ⎢HttpCodeA: ").append(httpCodeA);
//                }
            } else {
                if (siteContentB != null) {
                    String simpleBody = getBodySimple(siteContentB);
                    sb.append("\n⎢   ⎢BodyB: ").append(simpleBody, 0, Math.min(simpleBody.length(), 300));
                }
//                if (httpCodeB!=-1) {
//                    sb.append("\n⎢   ⎢HttpCodeB: ").append(httpCodeB);
//                }
            }

            sb.append("\n⎢   ⎢got:             [").append(testOutcome).append(        "]");
            sb.append("\n⎢   ⎣expected one of: ").append(Arrays.toString(expectedTestOutcome));
        }
        sb.append("\u001b[m"); //reset color
        return sb.toString();
    }

    /*
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
            sb.append("\n⎢   ⎡contains site A: ").append(this.requestBHttpContent.contains("site A")).append("]");
            sb.append("\n⎢   ⎣contains site B: ").append(this.requestBHttpContent.contains("site B")).append("]");
            String body = this.getBodySimple(this.requestBHttpContent);
            sb.append("\n⎢  [body: ").append(body);
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

        sb.append("\n⎣was that expected?:      ").append("?");
        sb.append("\u001b[m"); //reset color
        return sb.toString();
    }
    */

    private String getBodySimple(String content) {
        if (content.contains("<body>")) {
            content = content.split("<body>")[1].split("</body>")[0];
        }
        //body = body.replaceAll("<[^>]*>", "");
        content = content.replaceAll("\r", "");
        content = content.replaceAll("\n", " ");
        content = content.replaceAll("\t", " ");
        content = content.replaceAll("\s+", " ");
        return content;
    }

    private String getFullContent(WorkflowTrace trace) {
        StringBuilder fullBody = new StringBuilder();
        List<ProtocolMessage> messageList = WorkflowTraceUtil.getAllReceivedMessages(trace);
        List<ApplicationMessage> applicationMessages = messageList.stream()
                .filter(ApplicationMessage.class::isInstance)
                .map(ApplicationMessage.class::cast)
                .toList();
        for (ApplicationMessage message : applicationMessages) {
            fullBody.append(new String(message.getData().getValue(), StandardCharsets.UTF_8));
        }
        return fullBody.toString();
    }

    private int getHTTPCode(String text) {
        try {
            if (!text.startsWith("HTTP/1.1 ")) {
                return -1;
            } else {
                String code = text.split(" ")[1];
                return Integer.parseInt(code);
            }
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            LOGGER.warn("unable to parse HTTP code from '{}'", text.substring(0, Math.min(20, text.length())));
            return -1;
        }
    }
}
