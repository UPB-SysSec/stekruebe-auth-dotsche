package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import org.felix.thesis.sessionTickets.Ticket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * TestCases mainly contain the config and Workflow trace for the specific test
 */
public class TestCase {
    public TestCase(String name) {
        this.name = name;
    }
    private final String name;

    public String getName() {
        return name;
    }

    private Config buildConfig() {
        Config config = BaseConfigCreator.getConfig();
        return config;
    }

    /**
     * builds the State(config + workflow trace) for this test run
     * @param port the port to contact
     * @param siteADomain the domain of site A
     * @param siteACert the client cert for site A
     * @return the State object
     */
    public State getStateA(int port, String siteADomain, Path siteACert) {
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
     * @param siteBCert the client cert for site A
     * @param ticket the session Ticket to use for the resumption
     * @return the State object
     */
    public State getStateB(int port, String siteBDomain, Path siteBCert, Ticket ticket) {
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
