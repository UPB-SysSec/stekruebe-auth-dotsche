package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class BothCertsTestCase extends BaseTestCase{
    /**
     * 1. connects to site A (with client cert A) and requests a session Ticket
     * </br>
     * 2. connects to site B (with client cert B) and attaches the session Ticket to the request
     * </br>
     * Note: this test case cheats by giving site B its appropriate certificate.
     */
    public BothCertsTestCase(String name) {
        super(name);
        this.sendsCorrectCertToA = true;
        this.sendsCorrectCertToB = true;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        // get the usual stateA
        State state = super.getStateA(port, siteADomain, siteAClientCert);
        Config config = state.getConfig();
        WorkflowTrace trace = state.getWorkflowTrace();

        //apply the client cert
        config = this.applyCert(config, siteAClientCert);

        return new State(config, trace);
    }

    public State getStateB(int port, String siteBDomain, Path siteBClientCert, Ticket ticket) {
        // get usual stateB
        State state = super.getStateB(port, siteBDomain, siteBClientCert, ticket);
        Config config = state.getConfig();
        WorkflowTrace trace = state.getWorkflowTrace();

        // apply the client cert
        config = this.applyCert(config, siteBClientCert);

        return new State(config, trace);
    }

}
