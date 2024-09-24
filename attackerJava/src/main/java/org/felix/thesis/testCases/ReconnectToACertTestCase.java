package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
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
        this.certForA = CertificateChoice.A;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        // same as ReconnectToATestCase
        State ignore = super.getStateA(port, siteADomain, siteAClientCert);

        //basic connection config
        Config config = BaseConfigCreator.buildConfig(port, siteADomain);
        //apply the cert
        config = applyCert(config, siteAClientCert);
        //create a workflow-trace from the config
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        // return state
        return new State(config, trace);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        // same as ReconnectToATestCase
        State state = super.getStateB(port, savedSiteADomain, savedSiteAClientCert, ticket);

        //basic connection config
        Config config = state.getConfig();
        //apply the cert
//        config = applyCert(config, savedSiteAClientCert); //let's try without this
        //apply the ticket
        //create a workflow-trace from the config
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config);
        // return state
        return new State(config, trace);
    }
}
