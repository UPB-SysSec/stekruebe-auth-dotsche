package org.felix.thesis;

import com.google.common.primitives.Ints;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.protocol.message.*;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.ServerNameIndicationExtensionMessage;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.action.*;
import de.rub.nds.tlsattacker.core.state.State;

import javassist.bytecode.ByteArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        LOGGER.info("Hello world!");
        DockerResult res;

        if (false) {
            // build docker containers
            System.out.println("> building");
            Path p = Path.of("../setups/nginx/domains/dockerfile");
            res = DockerWrapper.build("nginx_domains", p);
            System.out.println(res.stdOut);
            System.out.println(res.stdErr);
            if (res.exitVal == -1) {
                System.err.println(res.toString());
                System.exit(-1);
            }
        }
        if (false) {
            // start docker containers
            System.out.println("> starting");
            res = DockerWrapper.run("nginx_domains", "nginx_domains");
            System.out.println(res.stdOut);
            System.out.println(res.stdErr);
            if (res.exitVal == -1) {
                System.err.println(res.toString());
                System.exit(-1);
            }
        }

        System.out.println("> running test");
        // run test

        Config config = new Config();
        config.setSupportedVersions(ProtocolVersion.TLS13);
        config.setDefaultSelectedProtocolVersion(ProtocolVersion.TLS13);
        config.setDefaultClientConnection(new OutboundConnection("client", 443, "localhost"));

        WorkflowTrace trace = new WorkflowTrace();

        // -- CLIENT HELLO --
        ClientHelloMessage chm = new ClientHelloMessage();
        //chm.setProtocolVersion(Ints.toByteArray(0x0303));
        //ServerNameIndicationExtensionMessage sniExt = new ServerNameIndicationExtensionMessage();
        //ServerNamePair snp = new ServerNamePair();
        //snp.setServerName("localhost".getBytes(StandardCharsets.US_ASCII));
        //sniExt.setServerNameList(Arrays.asList(snp));
        //chm.addExtension(sniExt);
        trace.addTlsAction(new SendAction(chm));


        // -- ALL OTHER MESSAGES --
        trace.addTlsAction(new ReceiveTillAction(new ServerHelloMessage()));
        trace.addTlsAction(new ReceiveTillAction(new CertificateMessage()));
        trace.addTlsAction(new ReceiveTillAction(new CertificateRequestMessage()));
        trace.addTlsAction(new ReceiveTillAction(new ServerHelloDoneMessage()));

        trace.addTlsAction(new SendDynamicClientKeyExchangeAction());
        trace.addTlsAction(new SendAction(new ChangeCipherSpecMessage()));
        trace.addTlsAction(new SendAction(new FinishedMessage()));

        trace.addTlsAction(new ReceiveTillAction(new ChangeCipherSpecMessage()));
        trace.addTlsAction(new ReceiveTillAction(new FinishedMessage()));

        trace.addTlsAction(new SendAction(new ApplicationMessage()));

        trace.addTlsAction(new ReceiveTillAction(new ApplicationMessage()));


        // -- RUN THE WORKFLOW --
        State state = new State(config, trace);
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
        executor.executeWorkflow();

        // -- FINISH UP --
        executor.closeConnection();
    }
}