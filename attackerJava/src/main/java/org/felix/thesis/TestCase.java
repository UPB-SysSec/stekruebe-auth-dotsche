package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;

/**
 * TestCases mainly contain the config and Workflow trace for the specific test
 */
public class TestCase {
    private String Name;

    public String getName() {
        return Name;
    }

    public State getState(int port) {
        Config config = BaseConfigCreator.getConfig();
        config.setDefaultClientConnection(new OutboundConnection(port));
        WorkflowTrace trace = BaseConfigCreator.getWorkflowTrace(config);

        return new State(config, trace);
    }
}
