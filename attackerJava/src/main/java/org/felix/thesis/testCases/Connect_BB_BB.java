package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;

public class Connect_BB_BB extends Connect_AA_BB {
    public Connect_BB_BB(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[] {
                TestOutcome.secondRequest_http200_contentB
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain);
        if (siteBNeedsCert) config = applyCert(config, siteBClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }

    //getStateB is the same as fow the base case, we just expect it to work this time
}
