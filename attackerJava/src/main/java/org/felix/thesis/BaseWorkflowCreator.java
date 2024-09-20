package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.AliasedConnection;
import de.rub.nds.tlsattacker.core.exceptions.ConfigurationException;
import de.rub.nds.tlsattacker.core.http.HttpMessage;
import de.rub.nds.tlsattacker.core.http.HttpRequestMessage;
import de.rub.nds.tlsattacker.core.http.HttpResponseMessage;
import de.rub.nds.tlsattacker.core.protocol.message.*;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.action.*;
import de.rub.nds.tlsattacker.core.workflow.action.executor.ActionOption;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowConfigurationFactory;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import de.rub.nds.tlsattacker.transport.ConnectionEndType;

import java.util.HashSet;
import java.util.Set;

public class BaseWorkflowCreator {

    public static WorkflowTrace createResumptionWorkflow(Config config) {
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

        // add the http message

        trace.addTlsAction(
                createHttpAction(
                        config,
                        connection,
                        ConnectionEndType.CLIENT,
                        new HttpRequestMessage(config))
        );
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


    public static WorkflowTrace getWorkflowTrace(Config config) {
        WorkflowConfigurationFactory factory = new WorkflowConfigurationFactory(config);
        WorkflowTrace trace = factory.createWorkflowTrace(config.getWorkflowTraceType(), config.getDefaultRunningMode());
        if (trace == null) {
            throw new ConfigurationException("Could not load workflow trace");
        } else {
            return trace;
        }
    }

    public static WorkflowTrace getResumptionWorkflow(Config config) {
        config.setWorkflowTraceType(WorkflowTraceType.RESUMPTION);
//        config.setWorkflowTraceType(WorkflowTraceType.TLS13_PSK);
        WorkflowTrace wf = getWorkflowTrace(config);
        AliasedConnection connection = config.getDefaultClientConnection();

        /*
        // transfer these options to the actions
        HashSet<ActionOption> globalOptions = new HashSet<>();
        if (config.getMessageFactoryActionOptions().contains(ActionOption.CHECK_ONLY_EXPECTED)) {
            globalOptions.add(ActionOption.CHECK_ONLY_EXPECTED);
        }
        if (config.getMessageFactoryActionOptions().contains(ActionOption.IGNORE_UNEXPECTED_WARNINGS)) {
            globalOptions.add(ActionOption.IGNORE_UNEXPECTED_WARNINGS);
        }

        SendAction actionSend = new SendAction(new HttpRequestMessage(config));
        actionSend.setConnectionAlias(connection.getAlias());
        actionSend.setActionOptions(globalOptions);
        wf.addTlsAction(actionSend);

        globalOptions.add(ActionOption.MAY_FAIL);
        ReceiveAction actionReceive = new ReceiveAction(new HttpResponseMessage(config));
        actionReceive.setConnectionAlias(connection.getAlias());
        actionReceive.setActionOptions(globalOptions);
        wf.addTlsAction(actionReceive);
        /**/

        wf.addTlsAction(new SendAsciiAction("GET / HTTP/2", "ASCII"));
        wf.addTlsAction(new ReceiveAction(new HttpResponseMessage()));
        return wf;
    }
}
