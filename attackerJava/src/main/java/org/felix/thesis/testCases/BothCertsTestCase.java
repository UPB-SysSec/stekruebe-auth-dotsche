package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
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
        this.certForA = CertificateChoice.A;
        this.certForB = CertificateChoice.B;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        //basic connection config
        Config config = BaseConfigCreator.buildConfig(port, siteADomain);
        //apply the cert
        config = applyCert(config, siteAClientCert);
        //create a workflow-trace from the config
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        // return state
        return new State(config, trace);
    }

    public State getStateB(int port, String siteBDomain, Path siteBClientCert, Ticket ticket) {
        //basic connection config
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain);
        //apply the cert
        config = applyCert(config, siteBClientCert);
        //create a workflow-trace from the config
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config);
        // return state
        return new State(config, trace);
    }

}
