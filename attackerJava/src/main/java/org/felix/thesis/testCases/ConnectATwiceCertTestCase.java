package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.sessionTickets.Ticket;

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
        this.certSentToA = CertificateChoice.A;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        super.getStateA(port, siteADomain, siteAClientCert);
        //basic connection config
        Config config = BaseConfigCreator.buildConfig(port, siteADomain);
        //apply the cert
        config = applyCert(config, siteAClientCert);
        //create a workflow-trace from the config
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config, siteADomain);
        // return state
        return new State(config, trace);
    }

    public State getStateB(int port, String siteBDomain, Path siteBClientCert, Ticket ticket) {
        return super.getStateA(port, savedSiteADomain, savedSiteAClientCert);
    }
}
