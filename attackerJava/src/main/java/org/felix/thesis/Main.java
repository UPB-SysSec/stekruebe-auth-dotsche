package org.felix.thesis;

import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.protocol.message.*;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.state.State;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.symmetric.ARC4;

import java.nio.file.Path;

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

        // -- CREATE CONFIG --
        Config config = BaseConfigCreator.getConfig();
        WorkflowTrace trace = BaseConfigCreator.getWorkflowTrace(config);

        // -- RUN THE WORKFLOW --
        State state = new State(config, trace);
        DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
        executor.executeWorkflow();

        // -- CLOSE CONNECTION --
        executor.closeConnection();

        WorkflowTrace wt = state.getWorkflowTrace();
        ApplicationMessage lm = wt.getLastReceivedMessage(ApplicationMessage.class);
        LOGGER.info(new String(lm.getData().getValue()));

        if (wt.executedAsPlanned()) {
            LOGGER.info("workflow success :)");
        } else {
            LOGGER.info("workflow fail :(");
        }

        // -- FINISH UP --
        System.out.println(state.getInboundContexts());
    }
}