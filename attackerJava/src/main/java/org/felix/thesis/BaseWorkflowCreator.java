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

import java.util.*;

public class BaseWorkflowCreator {
    private static final Logger LOGGER = LogManager.getLogger("WorkflowCreator");

    /**
     * creates the default workflow, usually used in the first connection
     * @param config the config file to base the workflow on
     * @return the finished workflow
     */
    public static WorkflowTrace getNormalWorkflowTraceALT(Config config) {
        config.setWorkflowTraceType(WorkflowTraceType.DYNAMIC_HTTPS);
        WorkflowConfigurationFactory factory = new WorkflowConfigurationFactory(config);
        WorkflowTrace trace = factory.createWorkflowTrace(config.getWorkflowTraceType(), config.getDefaultRunningMode());
        if (trace == null) {
            throw new ConfigurationException("Could not load workflow trace");
        } else {
//            AliasedConnection connection = config.getDefaultClientConnection();
//            //send the http message
//            trace.addTlsAction(
//                    createHttpAction(
//                            config,
//                            connection,
//                            ConnectionEndType.CLIENT,
//                            new HttpRequestMessage(config))
//            );
//            // receive the http answer
//            trace.addTlsAction(
//                    createHttpAction(
//                            config, connection, ConnectionEndType.SERVER, new HttpResponseMessage())
//            );
            return trace;
        }
    }

    /**
     * creates the default workflow, usually used in the first connection
     * @param config the config file to base the workflow on
     * @return the finished workflow
     */
    public static WorkflowTrace getNormalWorkflowTrace(Config config) {
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

        return trace;
    }

    public static WorkflowTrace getResumptionWorkflowTrace(Config config) {
        AliasedConnection connection = config.getDefaultClientConnection();
        WorkflowTrace trace = new WorkflowTrace();

        trace.addTlsAction(
                MessageActionFactory.createTLSAction(
                        config,
                        connection,
                        ConnectionEndType.CLIENT,
                        new ClientHelloMessage(config)));

        trace.addTlsAction(
                MessageActionFactory.createTLSAction(
                        config,
                        connection,
                        ConnectionEndType.SERVER,
                        new ServerHelloMessage(config),
                        new ChangeCipherSpecMessage(),
                        new FinishedMessage()));
        trace.addTlsAction(
                MessageActionFactory.createTLSAction(
                        config,
                        connection,
                        ConnectionEndType.CLIENT,
                        new ChangeCipherSpecMessage(),
                        new FinishedMessage()));

        // send the http message
        HttpRequestMessage reqMessage = new HttpRequestMessage(config);
        ArrayList<HttpHeader> headers = new ArrayList<>();
        HostHeader hostHeader = new HostHeader();
        hostHeader.setHeaderValue(config.getDefaultClientConnection().getHostname());
        headers.add(hostHeader);
        trace.addTlsAction(
                createHttpAction(
                        config,
                        connection,
                        ConnectionEndType.CLIENT,
                        reqMessage)
        );
        // receive the http answer
        trace.addTlsAction(
                createHttpAction(
                        config, connection, ConnectionEndType.SERVER, new HttpResponseMessage())
        );
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
