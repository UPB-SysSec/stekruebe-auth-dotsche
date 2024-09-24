package org.felix.thesis;

/*
Goal: run tests for a given config/setup
Steps:
    1. launch config (via docker)
    2. run all tests (optional parallel?)

*/

import de.rub.nds.tlsattacker.core.protocol.message.*;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.felix.thesis.sessionTickets.SessionTicketUtil;
import org.felix.thesis.sessionTickets.Ticket;
import org.felix.thesis.testCases.BaseTestCase;

public class TestSetupInstance {
    private final int port;
    private final List<BaseTestCase> tests;
    private final Path basePath;
    private final Path dockerFilePath;
    private final String siteADomain;
    private final String siteBDomain;
    private final Path siteACert;
    private final Path siteBCert;

    public String name;
    private final Logger LOGGER;

    private final boolean siteAUsesClientCert;
    private final boolean siteBUsesClientCert;

    private TestSetupResult result;

    /**
     * siteADomain, siteBDomain, siteACert(optional), siteBCert(optional)
     * @param port the port to run the docker container on
     * @param tests the list of TestCases
     * @param dockerFileFolder the folder containing the dockerFile
     */
    public TestSetupInstance(int port, List<BaseTestCase> tests, Path dockerFileFolder, String siteADomain, String siteBDomain, Boolean siteAUsesClientCert, Boolean siteBUsesClientCert) {
        this.port = port;
        this.tests = tests;
        this.basePath = dockerFileFolder;
        this.dockerFilePath = Paths.get(this.basePath.toString(), "dockerfile");
        this.siteADomain = siteADomain;
        this.siteBDomain = siteBDomain;
        this.siteACert = Path.of("../setups/shared/cert/keys/clientA.pem");
        this.siteBCert = Path.of("../setups/shared/cert/keys/clientB.pem");

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
        if (false) {
            // build docker containers
            LOGGER.info("building container");
            //Path p = Path.of("../setups/nginx/domains/dockerfile");
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
                    Thread.sleep(100L); //wait for the server to start
                } catch (InterruptedException ignore) {}
                LOGGER.info("> running successful");
            }
        }

        LOGGER.info("> running tests");
        for (BaseTestCase test : this.tests) {
            runTest(test);
        }

        LOGGER.info("stopping container");
        DockerWrapper.stop(this.name);
        //LOGGER.info("removing container");
        //DockerWrapper.remove(this.name);
    }


    public void runTest(BaseTestCase test) {
        boolean expectedToFail = test.getExpectedToFail(siteAUsesClientCert, siteBUsesClientCert);
        LOGGER.info("running test: '{}' on '{}', expected to fail: '{}'", test.getName(), name, expectedToFail);
        TestCaseResult testRes = new TestCaseResult(test.getName(), expectedToFail);

        // do first request
        State stateA = test.getStateA(this.port, this.siteADomain, this.siteACert);
        try {
            // ---- RUN THE WORKFLOW ----
            DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(stateA);
            executor.executeWorkflow();
            executor.closeConnection();
        } catch (Exception e) {
            testRes.requestAException = e;
        }
        testRes.requestAWorkflowExecutedAsPlanned = stateA.getWorkflowTrace().executedAsPlanned();
        testRes.requestAException = stateA.getExecutionException();
        testRes.requestAAlert = stateA.getWorkflowTrace().getLastReceivedMessage(AlertMessage.class);
        testRes.requestATrace = stateA.getWorkflowTrace();
        //testRes.requestAStatusCode = #TODO

        // get session ticket from request
        Ticket ticket = null;
        try {
            ticket = SessionTicketUtil.getSessionTickets(stateA).get(0);
        } catch (IndexOutOfBoundsException e) {
            testRes.requestAHadTicket = false;
        }

        if (ticket != null) { //only if we have a ticket
            // do second request (with the ticket from the first session)
            State stateB = test.getStateB(this.port, this.siteBDomain, this.siteBCert, ticket);
            try {
                // ---- RUN THE WORKFLOW ----
                DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(stateB);
                executor.executeWorkflow();
                executor.closeConnection();
            } catch (Exception e) {
                testRes.requestBException = e;
            }
            testRes.requestBWorkflowExecutedAsPlanned = stateB.getWorkflowTrace().executedAsPlanned();
            testRes.receivedDataFromB &= testRes.requestBWorkflowExecutedAsPlanned;
            testRes.requestBException = stateB.getExecutionException();
            ApplicationMessage appDataB = stateB.getWorkflowTrace().getFirstReceivedMessage(ApplicationMessage.class);
            if (appDataB!=null) {
                testRes.requestBApplicationData = appDataB;
                testRes.receivedDataFromB = true;
                testRes.requestBHttpStatusCode = getHTTPCode(appDataB.getData().getValue());
            }
            testRes.requestBAlert = stateB.getWorkflowTrace().getLastReceivedMessage(AlertMessage.class);
            testRes.requestBTrace = stateB.getWorkflowTrace();
            //testRes.requestBStatusCode = stateB... #TODO
        }

        result.results.add(testRes); // save testResult in setupResults
        if (testRes.receivedDataFromB ==testRes.expectedToFail) {result.allSuccessful = false;}
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
        String server = parts[2];
        String setup = parts[3];
        return server+"_"+setup;
    }

    private int getHTTPCode(byte[] appData) {
        String text = new String(appData, StandardCharsets.UTF_8);
        try {
            if (!text.startsWith("HTTP/1.1 ")) {
                return -1;
            } else {
                String code = text.split(" ")[1];
                return Integer.parseInt(code);
            }
        }catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            LOGGER.warn("unable to parse HTTP code from '{}'", text.substring(0, Math.min(20, text.length())));
            return -1;
        }
    }
}
