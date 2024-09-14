package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.constants.ClientAuthenticationType;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class BaseCertATestCase extends BaseTestCase{
    /**
     * 1. connects to site A (with client cert A) and requests a session Ticket
     * </br>
     * 2. connects to site B with the ticket
     */
    public BaseCertATestCase(String name) {
        super(name);
    }

    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        State state = super.getStateA(port, siteADomain, siteAClientCert);
        Config config = state.getConfig();
        WorkflowTrace trace = state.getWorkflowTrace();

        config.setClientAuthentication(true);
        config.setClientAuthenticationType(ClientAuthenticationType.CERTIFICATE_BASED);
        //#TODO how do i add the cert?

        return new State(config, trace);
    }
}
