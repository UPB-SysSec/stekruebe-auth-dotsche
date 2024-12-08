package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;

public class Connect_BB_BB_noCert extends Connect_BB_BB {
    public Connect_BB_BB_noCert(String name, ProtocolVersion version) {
        super(name, version);
        expectedTestOutcome = new TestOutcome[] {
                TestOutcome.firstRequest_tlsAlert_unexpectedMessage
        };
    }

    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain, version);
        //this test expects to fail at the first request
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        return new State(config, trace);
    }
    //getStateB is the same as fow the base case, we just expect it to work this time
}
