package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class Connect_A_nA extends Connect_A_BB {
    public Connect_A_nA(String name, ProtocolVersion version) {
        super(name, version);
        this.expectedTestOutcome = new TestOutcome[] {
                TestOutcome.secondRequest_noResumption,
                TestOutcome.secondRequest_http421_misdirectedRequest
        };
    }

    //getStateA is the same as for the base case

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain, false, version); //no SNI
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, siteADomain);
        return new State(config, trace);
    }
}