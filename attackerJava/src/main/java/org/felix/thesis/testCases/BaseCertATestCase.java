package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;

import java.nio.file.Path;

public class BaseCertATestCase extends BaseTestCase{
    /**
     * 1. connects to site A (with client cert A) and requests a session Ticket
     * </br>
     * 2. connects to site B with the ticket
     */
    public BaseCertATestCase(String name) {
        super(name);
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        //basic connection config
        Config config = BaseConfigCreator.buildConfig(port, siteADomain);
        //apply the cert
        config = applyCert(config, siteAClientCert);
        //create a workflow-trace from the config
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        // return state
        return new State(config, trace);
    }
}
