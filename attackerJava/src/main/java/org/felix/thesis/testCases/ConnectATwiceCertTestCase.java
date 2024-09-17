package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;

import java.nio.file.Path;

public class ConnectATwiceCertTestCase extends ConnectATwiceTestCase{
    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site A again (without any ticket)
     * </br>
     * Note: this test case is just a sanity check to make sure, the connections work at all
     */
    public ConnectATwiceCertTestCase(String name) {
        super(name);
        this.sendsCorrectCertToA = true;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        State state = super.getStateA(port, siteADomain, siteAClientCert);
        Config config = state.getConfig();
        WorkflowTrace wf = state.getWorkflowTrace();

        // apply the client cert for site A
        config = this.applyCert(config, siteAClientCert);

        return new State(config, wf);
    }
}
