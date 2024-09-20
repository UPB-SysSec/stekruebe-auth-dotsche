package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.layer.constant.LayerConfiguration;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseConfigCreator {
    /**
     * prepare the config
     * @param port the port to connect on
     * @param domain the domain to set in the SNI extension
     * @return the configured config
     */
    public static Config buildConfig(int port, String domain) {
        Config config = new Config();
        //config.setWorkflowTraceType(WorkflowTraceType.DYNAMIC_HTTPS);
        config.setDefaultLayerConfiguration(LayerConfiguration.HTTPS);


        // use session tickets
        config.setAddSessionTicketTLSExtension(true);


        // set connection type
        config.setDefaultClientConnection(new OutboundConnection("client", 443, "localhost"));


        // set http path
        config.setDefaultHttpsRequestPath("/");


        // set port
        config.setDefaultClientConnection(new OutboundConnection(port));


        // add SNI extension
        config.setAddServerNameIndicationExtension(true);
        ServerNamePair sn = new ServerNamePair(
                (byte) 0,
                domain.getBytes(StandardCharsets.US_ASCII)
        );
        config.setDefaultSniHostnames(List.of(sn));


        return config;
    }
}
