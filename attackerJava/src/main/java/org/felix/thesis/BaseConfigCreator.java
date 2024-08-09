package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.core.layer.constant.LayerConfiguration;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowConfigurationFactory;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseConfigCreator {
    public static Config getConfig() {
        String siteName = "SiteA.org";
        Config config = new Config();
        config.setWorkflowTraceType(WorkflowTraceType.HTTPS);

        // use TLS1.3 and HTTPS
        config.setSupportedVersions(ProtocolVersion.TLS13);
        config.setDefaultSelectedProtocolVersion(ProtocolVersion.TLS13);
        config.setDefaultLayerConfiguration(LayerConfiguration.HTTPS);

        // add SNI extension
        config.setAddServerNameIndicationExtension(true);
        ServerNamePair sn = new ServerNamePair(
                (byte) 0,
                siteName.getBytes(StandardCharsets.US_ASCII)
        );
        config.setDefaultSniHostnames(List.of(sn));


        // use session tickets
        config.setAddSessionTicketTLSExtension(true);

        config.setDefaultClientConnection(new OutboundConnection("client", 443, "localhost"));
        config.setDefaultHttpsRequestPath("/");
        return config;
    }

    public static WorkflowTrace getWorkflowTrace(Config config) {
        WorkflowConfigurationFactory factory = new WorkflowConfigurationFactory(config);
        WorkflowTrace trace = factory.createWorkflowTrace(config.getWorkflowTraceType(), config.getDefaultRunningMode());
        if (trace == null) {
            throw new ConfigurationException("Could not load workflow trace");
        } else {
            return trace;
        }
    }
}
