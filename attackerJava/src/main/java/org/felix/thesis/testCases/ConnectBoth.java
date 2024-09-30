package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.state.State;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ConnectBoth extends BaseTestCase{

    public ConnectBoth(String name) {
        super(name);
        this.doesSomethingIllegal = false; //connecting to a twice is not an illegal move
        this.certSentToA = CertificateChoice.None;
        this.certSentToB = CertificateChoice.None;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        return super.getStateA(port, siteADomain, siteAClientCert);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        return super.getStateA(port, siteBDomain, siteBCert);
    }
}
