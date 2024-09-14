package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.state.State;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ReconnectToACertTestCase extends BaseCertATestCase{
    String siteADomain;
    Path siteAClientCert;

    /**
     * 1. connects to site A (with client cert A) and requests a session Ticket
     * </br>
     * 2. connects to site A again (with client cert A) and attaches the session Ticket to the request
     * </br>
     * Note: this test case is just a sanity check to make sure, the connection resumption works at all
     */
    public ReconnectToACertTestCase(String name) {
        super(name);
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        this.siteAClientCert = siteAClientCert;
        this.siteADomain = siteADomain; //save for 2nd request

        //#TODO add cert to A

        return super.getStateA(port, siteADomain, siteAClientCert);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        return super.getStateB(port, this.siteADomain, this.siteAClientCert, ticket);
    }

}
