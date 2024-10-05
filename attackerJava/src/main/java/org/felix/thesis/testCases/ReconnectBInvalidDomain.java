package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class ReconnectBInvalidDomain extends TestReconnectToB{
    /**
     * we reconnect to site B, but the second request requests an invalid domain
     */
    public ReconnectBInvalidDomain(String name) {
        super(name);
        expectedTestOutcome =  new TestOutcome[]{
                TestOutcome.secondRequest_tlsAlert_internalError,
                TestOutcome.secondRequest_http421_misdirectedRequest,
                TestOutcome.secondRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http200_contentA
        };
    }

    // getStateA is the same as in TestReconnectToB

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, "unknownDomain.invalid");
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, "unknownDomain.invalid");
        return new State(config, trace);
    }
}
