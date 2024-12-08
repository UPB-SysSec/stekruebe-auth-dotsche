package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;

public class Connect_BA_BB extends RefTestCase {
    public Connect_BA_BB(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[]{
                TestOutcome.firstRequest_tlsAlert_unknownCA,
                TestOutcome.firstRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http400_badRequest
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain, version);
        if (siteANeedsCert) config = applyCert(config, siteAClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        return new State(config, trace);
    }

    //getStateB is as usual
}
