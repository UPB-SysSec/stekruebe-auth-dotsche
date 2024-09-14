package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ClientAuthenticationType;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ReconnectToATestCase extends BaseTestCase{
    String siteADomain;
    Path siteAClientCert;

    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site A again
     * </br>
     * Note: this test case is just a sanity check to make sure, the connection resumption works at all
     */
    public ReconnectToATestCase(String name) {
        super(name);
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        this.siteAClientCert = siteAClientCert;
        this.siteADomain = siteADomain; //save for 2nd request
        return super.getStateA(port, siteADomain, siteAClientCert);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        return super.getStateB(port, this.siteADomain, this.siteAClientCert, ticket);
    }
}
