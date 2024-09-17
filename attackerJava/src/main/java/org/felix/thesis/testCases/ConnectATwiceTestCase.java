package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.state.State;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ConnectATwiceTestCase extends BaseTestCase{
    String savedSiteADomain;
    Path savedSiteAClientCert;

    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site A again (without any ticket)
     * </br>
     * Note: this test case is just a sanity check to make sure, the connections work at all
     */
    public ConnectATwiceTestCase(String name) {
        super(name);
        this.doesSomethingIllegal = false; //connecting to a twice is not an illegal move
    }

    public boolean getExpectedToFail(boolean siteANeedsClientCert, boolean siteBNeedsClientCert) {
        return this.doesSomethingIllegal
                || siteANeedsClientCert && !this.sendsCorrectCertToA;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        //save for 2nd request
        this.savedSiteAClientCert = siteAClientCert;
        this.savedSiteADomain = siteADomain;

        return super.getStateA(port, siteADomain, siteAClientCert);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        // use the saved domain and cert for site B
        // note: (this does not actually use the certificate)
        return super.getStateA(port, savedSiteADomain, savedSiteAClientCert);
    }
}
