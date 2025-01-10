package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class Connect_A_BA extends RefTestCase {
    public Connect_A_BA(String name, ProtocolVersion version) {
        super(name, version);
        expectedTestOutcome = new TestOutcome[]{
                TestOutcome.secondRequest_noResumption,
                TestOutcome.secondRequest_http421_misdirectedRequest
        };
    }

    //getStateA is the same as in baseTestCase

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain, version);
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, siteADomain);
        return new State(config, trace);
    }
}
