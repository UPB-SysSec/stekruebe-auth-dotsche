package org.felix.thesis;

/*
Goal: run tests for a given config/setup
Steps:
    1. launch config (via docker)
    2. run all tests (optional parallel?)

*/

import de.rub.nds.tlsattacker.core.protocol.message.ApplicationMessage;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.DefaultWorkflowExecutor;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import org.felix.thesis.sessionTickets.SessionTicketUtil;
import org.felix.thesis.sessionTickets.Ticket;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TestSetupInstance {
    private final int port;
    private final List<TestCase> tests;
    private final Path basePath;
    private final Path dockerFilePath;
    private final String siteADomain;
    private final String siteBDomain;
    private final Path siteACert;
    private final Path siteBCert;

    public String name;
    private final Logger LOGGER;

    private TestSetupResult result;

    /**
     * siteAdomain, siteBdomain, siteAcert(optional), siteBcert(optional)
     * @param port the port to run the docker container on
     * @param tests the list of TestCases
     * @param dockerFileFolder the folder containing the dockerFile
     */
    public TestSetupInstance(int port, List<TestCase> tests, Path dockerFileFolder, String siteADomain, String siteBDomain, Boolean siteBUsesCert) {
        this.port = port;
        this.tests = tests;
        this.basePath = dockerFileFolder;
        this.dockerFilePath = Paths.get(this.basePath.toString(), "dockerfile");
        this.siteADomain = siteADomain;
        this.siteBDomain = siteBDomain;
        this.siteACert = Path.of("../setups/shared/cert/keys/clientA.pem");
        this.siteBCert = Path.of("../setups/shared/cert/keys/clientB.pem");

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
        result = new TestSetupResult(this.name);

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
                LOGGER.info("> running successful");
            }
        }

        LOGGER.info("> running tests");

        for (TestCase test : this.tests) { // #TODO this could be parallelized
            TestCaseResult testRes = new TestCaseResult(test.getName());

            // do first request
            State stateA = test.getStateA(this.port, this.siteADomain, this.siteACert);
            try {
                // ---- RUN THE WORKFLOW ----
                DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(stateA);
                executor.executeWorkflow();
                executor.closeConnection();
            } catch (Exception e) {
                testRes.exception = e;
                testRes.passed = false;
            }

            // get session ticket from request
            Ticket ticket = SessionTicketUtil.getSessionTickets(stateA).get(0);
            State stateB = test.getStateB(this.port, this.siteBDomain, this.siteBCert, ticket);

            // do second request (with the ticket from the first session)
            result.results.add(testRes); // save testResult in setupResults
            if (!testRes.passed) {result.allSuccessful = false;}
        }
        LOGGER.info("stopping container");
        DockerWrapper.stop(this.name);
        //LOGGER.info("removing container");
        //DockerWrapper.remove(this.name);
    }

    private executor()

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
}
