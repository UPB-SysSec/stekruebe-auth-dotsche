package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.file.Path;

public class ConnectBothCertA extends BaseTestCase{
    public ConnectBothCertA(String name) {
        super(name);
        this.doesSomethingIllegal = false; //connecting to a twice is not an illegal move
        this.certSentToA = CertificateChoice.A;
        this.certSentToB = CertificateChoice.None;
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        Config cfg = BaseConfigCreator.buildConfig(port, siteADomain);
        this.applyCert(cfg, siteAClientCert);
        WorkflowTrace wf = BaseWorkflowCreator.getNormalWorkflowTrace(cfg, siteADomain);
        return new State(cfg, wf);
    }

    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
        return super.getStateA(port, siteBDomain, siteBCert);
    }
}
