package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.state.State;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class InvalidDomainBTestCase extends BaseTestCase{
    /**
     * 1. connects to site A (with client cert A) and requests a session Ticket
     * </br>
     * 2. connects to site B with the ticket
     */
    public InvalidDomainBTestCase(String name) {
        super(name);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        return super.getStateA(port, "unknownSite.org", siteBCert);
    }
}
