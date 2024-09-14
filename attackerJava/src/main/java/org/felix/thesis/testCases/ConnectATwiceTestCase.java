package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.action.MessageAction;
import de.rub.nds.tlsattacker.core.workflow.action.SendAction;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ConnectATwiceTestCase extends BaseTestCase{
    String siteADomain;
    Path siteAClientCert;

    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site A again (without any ticket)
     * </br>
     * Note: this test case is just a sanity check to make sure, the connections work at all
     */
    public ConnectATwiceTestCase(String name) {
        super(name);
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        this.siteAClientCert = siteAClientCert;
        this.siteADomain = siteADomain; //save for 2nd request
        return super.getStateA(port, siteADomain, siteAClientCert);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        return super.getStateA(port, this.siteADomain, this.siteAClientCert);
    }
}
