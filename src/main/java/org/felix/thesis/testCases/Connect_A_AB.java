package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class Connect_A_AB extends RefTestCase {
    public Connect_A_AB(String name, ProtocolVersion version) {
        super(name, version);
        expectedTestOutcome = new TestOutcome[]{
                TestOutcome.secondRequest_http200_contentA,
                TestOutcome.secondRequest_http421_misdirectedRequest,
                TestOutcome.secondRequest_http403_forbidden
        };
    }

    //getStateA is the same as in baseTestCase

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, siteADomain, version);
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }
}
