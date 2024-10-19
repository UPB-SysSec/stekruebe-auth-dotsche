package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;

public class Connect_AB_BB extends Connect_AA_BB {
    public Connect_AB_BB(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[]{
                TestOutcome.secondRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http421_misdirectedRequest
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteADomain);
        if (siteANeedsCert) config = applyCert(config, siteAClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }

    //getStateB is as usual

}
