package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

public class BaseTestCase {
    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site B and attaches the session Ticket to the request
     * </br>
     * Note: this test case always fails for setups that require a certificate on siteA
     */
    public BaseTestCase(String name) {
        this.name = name;
    }
    private final String name;

    public String getName() {
        return name;
    }

    private Config buildConfig() {
        return BaseConfigCreator.getConfig();
    }

    /**
     * builds the State(config + workflow trace) for this test run
     * @param port the port to contact
     * @param siteADomain the domain of site A
     * @param siteAClientCert the client cert for site A
     * @return the State object
     */
    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        Config config = this.buildConfig();

        // set port
        config.setDefaultClientConnection(new OutboundConnection(port));

        // add SNI extension
        config.setAddServerNameIndicationExtension(true);
        ServerNamePair sn = new ServerNamePair(
                (byte) 0,
                siteADomain.getBytes(StandardCharsets.US_ASCII)
        );
        config.setDefaultSniHostnames(List.of(sn));

        WorkflowTrace trace = BaseConfigCreator.getWorkflowTrace(config);
        return new State(config, trace);
    }

    /**
     * builds the State(config + workflow trace) for this test run
     * @param port the port to contact
     * @param siteBDomain the domain of site A
     * @param siteBClientCert the client cert for site A
     * @param ticket the session Ticket to use for the resumption
     * @return the State object
     */
    public State getStateB(int port, String siteBDomain, Path siteBClientCert, Ticket ticket) {
        Config config = this.buildConfig();

        // set port
        config.setDefaultClientConnection(new OutboundConnection(port));

        config.setWorkflowTraceType(WorkflowTraceType.TLS13_PSK);

        // add SNI extension
        config.setAddServerNameIndicationExtension(true);
        ServerNamePair sn = new ServerNamePair(
                (byte) 0,
                siteBDomain.getBytes(StandardCharsets.US_ASCII)
        );
        config.setDefaultSniHostnames(List.of(sn));

        // add session ticket
        ticket.applyTo(config);

        // return state
        WorkflowTrace trace = BaseConfigCreator.getWorkflowTrace(config);
        return new State(config, trace);
    }
}
