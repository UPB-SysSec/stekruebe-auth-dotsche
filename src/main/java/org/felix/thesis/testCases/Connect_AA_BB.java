package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import org.felix.thesis.TestOutcome;

public class Connect_AA_BB extends RefTestCase {

    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site B and attaches the session Ticket to the request
     * </br>
     * Note: this test case always fails for setups that require a certificate on siteA
     */
    public Connect_AA_BB(String name, ProtocolVersion version) {
        super(name, version);
        this.expectedTestOutcome = new TestOutcome[] {
                TestOutcome.secondRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http421_misdirectedRequest
        };
    }

    //getStateA is the same as for the base case

    //getStateB is the same as for the base case

}