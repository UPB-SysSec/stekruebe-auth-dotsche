package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;

public class Connect_BB_BB_CertA extends Connect_BB_BB {
    public Connect_BB_BB_CertA(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[] {
                TestOutcome.firstRequest_tlsAlert_unknownCA
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain, version);
        if (siteBNeedsCert) config = applyCert(config, siteAClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        return new State(config, trace);
    }

    //getStateB is the same as fow the base case, we just expect it to work this time
}
