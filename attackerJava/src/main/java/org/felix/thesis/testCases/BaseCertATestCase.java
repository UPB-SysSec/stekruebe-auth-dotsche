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
        this.sendsCorrectCertToA = true;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        // get usual stateA
        State state = super.getStateA(port, siteADomain, siteAClientCert);
        Config config = state.getConfig();
        WorkflowTrace trace = state.getWorkflowTrace();

        //add the cert
        config = this.applyCert(config, siteAClientCert);

        return new State(config, trace);
    }
}
