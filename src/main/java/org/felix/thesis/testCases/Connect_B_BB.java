package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;

public class Connect_B_BB extends Connect_B_BB_test {
    public Connect_B_BB(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[] {
                TestOutcome.firstRequest_tlsAlert_unexpectedMessage
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain);
        //this test expects to fail at the first request
        //because we do not send the necessary client cert for site B
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }
    //getStateB is the same as fow the base case, we just expect it to work this time
}
