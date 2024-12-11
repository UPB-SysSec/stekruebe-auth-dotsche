package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class Connect_A_XX extends RefTestCase {
    /**
     * we reconnect to site A, but the second request requests an invalid domain
     */
    public Connect_A_XX(String name, ProtocolVersion version) {
        super(name, version);
        expectedTestOutcome = new TestOutcome[]{
                TestOutcome.secondRequest_tlsAlert_internalError,
                TestOutcome.secondRequest_http421_misdirectedRequest,
                TestOutcome.secondRequest_http404_notFound,
                TestOutcome.secondRequest_tlsAlert_unexpectedMessage
        };
    }

    // getStateA is the same as in TestReconnectToA

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, "unknownDomain.invalid", version);
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, "unknownDomain.invalid");
        return new State(config, trace);
    }
}
