package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class Connect_BB_XX extends Connect_BB_BB {
    /**
     * we reconnect to site B, but the second request requests an invalid domain
     */
    public Connect_BB_XX(String name) {
        super(name);
        expectedTestOutcome =  new TestOutcome[]{
                TestOutcome.secondRequest_tlsAlert_internalError,
                TestOutcome.secondRequest_http421_misdirectedRequest,
                TestOutcome.secondRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http404_notFound,
                TestOutcome.secondRequest_http200_contentB
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain);
        if (siteBNeedsCert) config = applyCert(config, siteBClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, "unknownDomain.invalid");
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, "unknownDomain.invalid");
        return new State(config, trace);
    }
}
