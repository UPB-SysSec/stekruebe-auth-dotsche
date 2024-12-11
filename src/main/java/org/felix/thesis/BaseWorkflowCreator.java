package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.AliasedConnection;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.core.http.HttpMessage;
import de.rub.nds.tlsattacker.core.http.HttpRequestMessage;
import de.rub.nds.tlsattacker.core.http.HttpResponseMessage;
import de.rub.nds.tlsattacker.core.http.header.HttpHeader;
import de.rub.nds.tlsattacker.core.http.header.HostHeader;
import de.rub.nds.tlsattacker.core.protocol.message.*;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.action.*;
import de.rub.nds.tlsattacker.core.workflow.action.executor.ActionOption;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowConfigurationFactory;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.felix.thesis.custom.HostHeaderCustom;

import java.util.*;

public class BaseWorkflowCreator {
    private static final Logger LOGGER = LogManager.getLogger("WorkflowCreator");
    /**
     * creates the default workflow, usually used in the first connection
     * @param config the config file to base the workflow on
     * @return the finished workflow
     */
    public static WorkflowTrace getNormalWorkflowTrace(Config config, String domain) {
        AliasedConnection connection = config.getDefaultClientConnection();
        config.setWorkflowTraceType(WorkflowTraceType.DYNAMIC_HTTPS);

        WorkflowTrace trace = new WorkflowTrace();
        trace.addTlsAction(
                MessageActionFactory.createTLSAction(config, connection, ConnectionEndType.CLIENT, new ClientHelloMessage(config))
        );

        if (config.getHighestProtocolVersion().isTLS13()) {
            trace.addTlsAction(new ReceiveTillAction(new FinishedMessage()));
        } else {
            trace.addTlsAction(new ReceiveTillAction(new ServerHelloDoneMessage()));
        }


        if (Objects.equals(config.isClientAuthentication(), Boolean.TRUE)) {
            trace.addTlsAction(new SendAction(new CertificateMessage()));
            trace.addTlsAction(new SendDynamicClientKeyExchangeAction());
            trace.addTlsAction(new SendAction(new CertificateVerifyMessage()));
        } else {
            trace.addTlsAction(new SendDynamicClientKeyExchangeAction());
        }

        trace.addTlsAction(new SendAction(new ChangeCipherSpecMessage(), new FinishedMessage()));
        trace.addTlsAction(new ReceiveTillAction(new FinishedMessage()));

        // send the http message
        trace.addTlsAction(
                createHttpAction(
                        config,
                        connection,
                        ConnectionEndType.CLIENT,
                        buildHTTPRequestMessage(config, domain))
        );
        // receive the http answer
        trace.addTlsAction(
                createHttpAction(
                        config, connection, ConnectionEndType.SERVER, new HttpResponseMessage())
        );

        return trace;
    }

    public static WorkflowTrace getNormalWorkflowTrace_13(Config config, String domain) {
        LOGGER.info("domain for 1.3 workflow: {}", domain);
        AliasedConnection connection = config.getDefaultClientConnection();
        WorkflowTrace trace = new WorkflowTrace();


        trace.addTlsAction(new SendAction(new ClientHelloMessage(config)));

        trace.addTlsAction(new ReceiveTillAction(new FinishedMessage()));

        trace.addTlsAction(new SendAction(
                new ChangeCipherSpecMessage(),
                new FinishedMessage()
        ));
        // send the http message
        trace.addTlsAction(createHttpAction(
                        config,
                        connection,
                        ConnectionEndType.CLIENT,
                        buildHTTPRequestMessage(config, domain)
                ));
        // receive the http answer
        trace.addTlsAction(createHttpAction(
                        config,
                        connection,
                        ConnectionEndType.SERVER,
                        new HttpResponseMessage()
                ));
        return trace;
    }


    private static HttpRequestMessage buildHTTPRequestMessage(Config config, String domain) {
        HttpRequestMessage reqMessage = new HttpRequestMessage(config);
        ArrayList<HttpHeader> headers = new ArrayList<>();
        HostHeader hostHeader = new HostHeader();
        hostHeader.setHeaderValue(domain);
        headers.add(hostHeader);
        reqMessage.setHeader(headers);
        return reqMessage;
    }


    public static WorkflowTrace getResumptionWorkflowTrace(Config config, String domain) {
        LOGGER.info("domain for workflow: {}", domain);
        AliasedConnection connection = config.getDefaultClientConnection();
        WorkflowTrace trace = new WorkflowTrace();

        trace.addTlsAction(new SendAction(new ClientHelloMessage(config)));
        trace.addTlsAction(new ReceiveTillAction(new FinishedMessage()));
        trace.addTlsAction(new SendAction(
                new ChangeCipherSpecMessage(),
                new FinishedMessage()
        ));
        // send the http request
        trace.addTlsAction(createHttpAction(
                        config,
                        connection,
                        ConnectionEndType.CLIENT,
                        buildHTTPRequestMessage(config, domain)
                ));
        // receive the http answer
        trace.addTlsAction(createHttpAction(
                        config,
                        connection,
                        ConnectionEndType.SERVER,
                        new HttpResponseMessage()
                ));
        return trace;
    }




    private static MessageAction createHttpAction(
            Config tlsConfig,
            AliasedConnection connection,
            ConnectionEndType sendingConnectionEndType,
            HttpMessage httpMessages) {
        MessageAction action;
        if (connection.getLocalConnectionEndType() == sendingConnectionEndType) {
            action = new SendAction(httpMessages);
        } else {
            action = new ReceiveAction(httpMessages);
            action.setActionOptions(getFactoryReceiveActionOptions(tlsConfig));
        }
        action.setConnectionAlias(connection.getAlias());
        return action;
    }

    private static Set<ActionOption> getFactoryReceiveActionOptions(Config tlsConfig) {
        Set<ActionOption> globalOptions = new HashSet<>();
        if (tlsConfig.getMessageFactoryActionOptions().contains(ActionOption.CHECK_ONLY_EXPECTED)) {
            globalOptions.add(ActionOption.CHECK_ONLY_EXPECTED);
        }
        if (tlsConfig
                .getMessageFactoryActionOptions()
                .contains(ActionOption.IGNORE_UNEXPECTED_WARNINGS)) {
            globalOptions.add(ActionOption.IGNORE_UNEXPECTED_WARNINGS);
        }

        return globalOptions;
    }

}
