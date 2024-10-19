package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;

public class Connect_BA_BA extends Connect_AA_BB {
    public Connect_BA_BA(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[]{
                TestOutcome.firstRequest_tlsAlert_unknownCA,
                TestOutcome.firstRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http400_badRequest
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain);
        if (siteANeedsCert) config = applyCert(config, siteAClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config, siteADomain);
        return new State(config, trace);
    }

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain);
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, siteADomain);
        return new State(config, trace);
    }
}
