package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;

public class TestReconnectToB_NoCert extends BaseTestCase{
    public TestReconnectToB_NoCert(String name) {
        super(name);
        expectedTestOutcome = new TestOutcome[] {
                TestOutcome.firstRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http200_contentB
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain);
        //if (siteBNeedsCert) config = applyCert(config, siteBClientCert); //this test expects to fail at the first request
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }
    //getStateB is the same as fow the base case, we just expect it to work this time
}
