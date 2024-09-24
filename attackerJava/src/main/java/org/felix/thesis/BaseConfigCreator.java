package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.layer.constant.LayerConfiguration;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class BaseConfigCreator {
    private static final Logger LOGGER = LogManager.getLogger("configCreator");
    /**
     * prepare the config
     * @param port the port to connect on
     * @param domain the domain to set in the SNI extension
     * @return the configured config
     */
    public static Config buildConfig(int port, String domain) {
        if (domain == null) throw new AssertionError();
        LOGGER.info("domain: {}", domain);

        Config config = new Config();
        config.setDefaultLayerConfiguration(LayerConfiguration.HTTPS);

        // use session tickets
        config.setAddSessionTicketTLSExtension(true);


        // set connection type
        config.setDefaultClientConnection(new OutboundConnection("client", port, "127.0.0.2"));


        // set http path
        config.setDefaultHttpsRequestPath("/");


        // set port
//        config.setDefaultClientConnection(new OutboundConnection(port));
        LOGGER.info("outbound connection to port {}", port);

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
