package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ReconnectToACertTestCase extends ReconnectToATestCase{
    /**
     * 1. connects to site A (with client cert A) and requests a session Ticket
     * </br>
     * 2. connects to site A again (with client cert A) and attaches the session Ticket to the request
     * </br>
     * Note: this test case is just a sanity check to make sure, the connection resumption works at all
     */
    public ReconnectToACertTestCase(String name) {
        super(name);
        this.sendsCorrectCertToA = true;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        // same as ReconnectToATestCase
        State state = super.getStateA(port, siteADomain, siteAClientCert);

        // apply the certificate for site A
        Config config = state.getConfig();
        WorkflowTrace wf = state.getWorkflowTrace();
        config = this.applyCert(config, siteAClientCert);

        return new State(config, wf);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        // same as ReconnectToATestCase
        State state = super.getStateB(port, savedSiteADomain, savedSiteAClientCert, ticket);

        // apply the certificate for site A
        Config config = state.getConfig();
        WorkflowTrace wf = state.getWorkflowTrace();
        config = this.applyCert(config, savedSiteAClientCert);

        return new State(config, wf);
    }
}
