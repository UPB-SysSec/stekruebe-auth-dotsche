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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class TestSetupInstance {
    private final int port;
    private final List<TestCase> tests;
    private final Path dockerFilePath;
    public String name;
    private final Logger LOGGER;

    private TestSetupResult result;

    /**
     *
     * @param port the port to run the docker container on
     * @param tests the list of TestCases
     * @param dockerFileFolder the folder containing the dockerFile
     */
    public TestSetupInstance(int port, List<TestCase> tests, Path dockerFileFolder) {
        this.port = port;
        this.tests = tests;
        this.dockerFilePath = Paths.get(dockerFileFolder.toString(), "dockerfile");
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
        if (true) {
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
            State state = test.getState(this.port);

            try {
                // ---- RUN THE WORKFLOW ----
                DefaultWorkflowExecutor executor = new DefaultWorkflowExecutor(state);
                executor.executeWorkflow();
                executor.closeConnection();


                // ---- GET RESULTS ----
                WorkflowTrace wt = state.getWorkflowTrace();
                ApplicationMessage lm = wt.getLastReceivedMessage(ApplicationMessage.class);
            } catch (Exception e) {
                testRes.exception = e;
                testRes.passed = false;
            }
            result.results.add(testRes); // save testResult in setupResults
            if (!testRes.passed) {result.allSuccessful = false;}
        }
        LOGGER.info("stopping container");
        DockerWrapper.stop(this.name);
        //LOGGER.info("removing container");
        //DockerWrapper.remove(this.name);
    }

    /**
     * getter for the results
     * @return the results
     */
    public TestSetupResult getResults() {
        return this.result;
    }

    private String _getName() {
        String[] parts = this.dockerFilePath.toString().toLowerCase(Locale.ENGLISH).split("/");
        String server = parts[2];
        String setup = parts[3];
        return server+"_"+setup;
    }
}
