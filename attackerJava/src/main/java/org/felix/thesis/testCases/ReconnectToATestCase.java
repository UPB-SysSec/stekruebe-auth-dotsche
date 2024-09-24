package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.state.State;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ReconnectToATestCase extends BaseTestCase{
    String savedSiteADomain;
    Path savedSiteAClientCert;

    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site A again
     * </br>
     * Note: this test case is just a sanity check to make sure, the connection resumption works at all
     */
    public ReconnectToATestCase(String name) {
        super(name);
        this.doesSomethingIllegal = false; //reconnecting to siteA with a ticket for siteA is not illegal
    }

    /**
     * return true if we: </br>
     *    1. do something illegal or </br>
     *    2. siteA needs the correct cert, and we don't send it or </br>
     *    3. siteB needs the correct cert, and we don't send it
     * @return whether the test is expected to go smoothly or have the server fail
     */
    public boolean getExpectedToFail(boolean siteANeedsClientCert, boolean siteBNeedsClientCert) {
        if (this.doesSomethingIllegal) return true;
        if (this.certForA != (siteANeedsClientCert ? CertificateChoice.A : CertificateChoice.None)) return true;
        return false;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        this.savedSiteAClientCert = siteAClientCert;
        this.savedSiteADomain = siteADomain; //save for 2nd request

        return super.getStateA(port, siteADomain, siteAClientCert);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        // just use the ticket from siteA to connect to siteA again
        return super.getStateB(port, savedSiteADomain, savedSiteAClientCert, ticket);
    }
}
