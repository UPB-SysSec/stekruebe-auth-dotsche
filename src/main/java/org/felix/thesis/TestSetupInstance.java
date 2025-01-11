package org.felix.thesis;

/*
Goal: run tests for a given config/setup
Steps:
    1. launch config (via docker)
    2. run all tests (optional parallel?)

*/

import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowExecutorFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.felix.thesis.sessionTickets.SessionTicketUtil;
import org.felix.thesis.sessionTickets.Ticket;
import org.felix.thesis.testCases.RefTestCase;

public class TestSetupInstance {
    private final int port;
    private final List<RefTestCase> tests;

    private final Path dockerFilePath;
    private final String siteADomain;
    private final String siteBDomain;
    private final Path siteACert;
    private final Path siteBCert;
    private final boolean siteAUsesClientCert;
    private final boolean siteBUsesClientCert;

    public String name;
    private final Logger LOGGER;


    private TestSetupResult result;

    /**
     * siteADomain, siteBDomain, siteACert(optional), siteBCert(optional)
     * @param port the port to run the docker container on
     * @param tests the list of TestCases
     * @param dockerFileFolder the folder containing the dockerFile
     */
    public TestSetupInstance(int port, List<RefTestCase> tests, Path dockerFileFolder, String siteADomain, String siteBDomain, Boolean siteAUsesClientCert, Boolean siteBUsesClientCert) {
        this.port = port;
        this.tests = tests;
        this.dockerFilePath = Paths.get(dockerFileFolder.toString(), "dockerfile");
        this.siteADomain = siteADomain;
        this.siteBDomain = siteBDomain;
        this.siteACert = Path.of("./setups/shared/cert/keys/clientA.pem");
        this.siteBCert = Path.of("./setups/shared/cert/keys/clientB.pem");

        this.siteAUsesClientCert = siteAUsesClientCert;
        this.siteBUsesClientCert = siteBUsesClientCert;

        this.name = this._getName();
        this.LOGGER = LogManager.getLogger(this.name);
    }

    /**
     * runs the tests and saves the results in this.result
     */
    public void runTests() {
        /*
            # populates this.result

            docker.start(dockerFile, port)
            for test in tests:
                res = test.run(this.port)
                this.result.addResult(res)
            docker.stop()
        */
        result = new TestSetupResult(this.name, this.port);

        /* ------ Build Docker Container ------ */
        if (true) {
            // build docker containers
            LOGGER.info("building container");
            //Path p = Path.of("./setups/nginx/domains/dockerfile");
            try {
                result.dockerBuildResult =  DockerWrapper.build(this.name, this.dockerFilePath);
                result.dockerBuildSuccessful = (result.dockerBuildResult.exitVal == 0);
            } catch (Exception e) {
                result.dockerBuildException = e;
                result.dockerBuildSuccessful = false;
            }
            if (!result.dockerBuildSuccessful) {
                if (result.dockerBuildException != null) {
                    LOGGER.error("BUILD FAILED {}", result.dockerBuildException.toString());
                } else {
                    LOGGER.error("BUILD FAILED ({}): {}", result.dockerBuildResult.exitVal, result.dockerBuildResult.stdErr);
                }
                return; //cannot continue if build fails
            } else {
                LOGGER.info("> building successful");
            }
        }
        /* ------ Run Docker containers ------ */
        if (true) {
            // start docker containers
            LOGGER.info("starting container on port {}", this.port);
            try {
                result.dockerRunResult = DockerWrapper.run(this.name, this.name, true, this.port);
                result.dockerRunSuccessful = (result.dockerRunResult.exitVal == 0);
            } catch (Exception e) {
                result.dockerRunException = e;
                result.dockerRunSuccessful = false;
            }
            if (!result.dockerRunSuccessful) {
                LOGGER.error("UNABLE TO RUN DOCKER IMAGE");
                if (result.dockerRunException != null) {
                    LOGGER.error("got exception {}", result.dockerRunException);
                }
                if (result.dockerRunResult.exitVal!=0) {
                    LOGGER.error("finished with non-zero exit value: {} | {}", result.dockerRunResult.exitVal, result.dockerRunResult.stdErr);
                    LOGGER.error("dbg: {}", result.dockerRunResult.debugMessage);
                }
                return; //cannot continue if run fails
            } else {
                try {
                    LOGGER.info("waiting for server to start");
                    Thread.sleep(10000L); //wait for the server to start
                } catch (InterruptedException ignore) {}
                LOGGER.info("> running successful");
            }
        }
        /* ------ make Test connections ------ */
        LOGGER.info("> running tests");
        for (RefTestCase test : this.tests) {
            runTest(test);
        }
        /* ------ Stop Docker Containers ------ */
        LOGGER.info("stopping container");
        DockerWrapper.stop(this.name);
        //LOGGER.info("removing container");
        //DockerWrapper.remove(this.name);
    }


    public void runTest(RefTestCase test) {
        LOGGER.info("running test: '{}' on '{}'", test.getName(), name);
        test.setup(
                port,
                siteAUsesClientCert,
                siteBUsesClientCert,
                siteADomain,
                siteBDomain,
                siteACert,
                siteBCert
        );
        TestCaseResult testRes = new TestCaseResult(test.getName());
        testRes.expectedTestOutcome = test.expectedTestOutcome;
        // do first request
        State stateA = test.getStateA();
        try {
            // ---- RUN THE WORKFLOW ----
//            DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(stateA);
            WorkflowExecutor executor = WorkflowExecutorFactory.createWorkflowExecutor(stateA.getConfig().getWorkflowExecutorType(), stateA);
            executor.executeWorkflow();
//            executor.sendCloseNotify();
//            executor.closeConnection();
        } catch (Exception e) {
            testRes.requestAException = e;
        }
        if (stateA.getExecutionException() != null) {
            testRes.requestAException = stateA.getExecutionException();
        }
        testRes.requestATrace = stateA.getWorkflowTrace();

        // get session ticket from request
        Ticket ticket = null;
        try {
            ticket = SessionTicketUtil.getSessionTickets(stateA).get(0);
        } catch (IndexOutOfBoundsException ignore) {} //no ticket

        if (ticket != null) { //only if we have a ticket
            testRes.executedRequestB = true;
            // do second request (with the ticket from the first session)
            State stateB = test.getStateB(ticket);
            try {
                // ---- RUN THE WORKFLOW ----
                DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(stateB);
                executor.executeWorkflow();
//                executor.sendCloseNotify();
//                executor.closeConnection();
            } catch (Exception e) {
                testRes.requestBException = e;
            }
            if (stateB.getExecutionException() != null) {
                testRes.requestBException = stateB.getExecutionException();
            }
            testRes.requestBTrace = stateB.getWorkflowTrace();
        }
        if (!testRes.wasOutcomeExpected()) result.allAsExpected = false; //if any one test result wasn't expected, set to false
        result.results.add(testRes); // save testResult in setupResults
    }

    /**
     * getter for the results
     * @return the results
     */
    public TestSetupResult getResults() {
        return this.result;
    }

    /**
     * builds a human-readable name from the config paths
     * @return the new name
     */
    private String _getName() {
        String[] parts = this.dockerFilePath.toString().toLowerCase(Locale.ENGLISH).split("/");
        String server = parts[parts.length - 3];
        String setup = parts[parts.length - 2];
        return server+"_"+setup;
    }
}
