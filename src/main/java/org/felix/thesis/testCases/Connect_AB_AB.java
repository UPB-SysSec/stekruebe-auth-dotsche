package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class Connect_AB_AB extends RefTestCase {
    public Connect_AB_AB(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[]{
                TestOutcome.secondRequest_http200_contentA
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteADomain, version);
        if (siteANeedsCert) config = applyCert(config, siteAClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        return new State(config, trace);
    }

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, siteADomain, version);
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }

}
