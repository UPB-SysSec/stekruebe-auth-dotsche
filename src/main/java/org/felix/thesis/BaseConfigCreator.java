package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.layer.constant.LayerConfiguration;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseConfigCreator {
    private static final Logger LOGGER = LogManager.getLogger("configCreator");

    public static Config buildConfig(int port, String domain, ProtocolVersion version) {
        return buildConfig(port, domain, true, version);
    }
    /**
     * prepare the config
     * @param port the port to connect on
     * @param domain the domain to set in the SNI extension
     * @return the configured config
     */
    public static Config buildConfig(int port, String domain, boolean useSNI, ProtocolVersion version) {
        if (domain == null) throw new AssertionError();
        LOGGER.info("domain: {}", domain);

        Config config = new Config();
        config.setWriteKeylogFile(true);
        config.setKeylogFilePath("/tmp/keylogfile");
        config.setWorkflowTraceType(WorkflowTraceType.DYNAMIC_HTTPS);
        config.setDefaultLayerConfiguration(LayerConfiguration.HTTPS);

        // use session tickets
        config.setAddSessionTicketTLSExtension(true);

        if (version != null) {
            config.setDefaultSelectedProtocolVersion(version);
            config.setHighestProtocolVersion(version);
        } else {
            config.setDefaultSelectedProtocolVersion(ProtocolVersion.TLS12);
            config.setHighestProtocolVersion(ProtocolVersion.TLS12);
        }
        if(config.getHighestProtocolVersion().isTLS13()) {
            config.setAddSupportedVersionsExtension(true);
            config.setAddExtendedMasterSecretExtension(false);
            config.setAddEncryptThenMacExtension(false);
            config.setAddSupportedVersionsExtension(true);
            config.setAddKeyShareExtension(true);
            config.setAddPSKKeyExchangeModesExtension(true);
//            config.setAddPreSharedKeyExtension(true);
        }


        // set connection type
        var connection = new OutboundConnection("client", port, domain);
        connection.setIp("127.0.0.2");
        config.setDefaultClientConnection(connection);


        // set http path
        config.setDefaultHttpsRequestPath("/");


        // set port
//        config.setDefaultClientConnection(new OutboundConnection(port));
        LOGGER.info("outbound connection to port {}", port);

        if (useSNI) {
            // add SNI extension
            config.setAddServerNameIndicationExtension(true);
            ServerNamePair sn = new ServerNamePair(
                    (byte) 0,
                    domain.getBytes(StandardCharsets.US_ASCII)
            );
            config.setDefaultSniHostnames(List.of(sn));
        }

        return config;
    }
}
