package org.felix.thesis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.felix.thesis.testCases.BaseTestCase;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
Goal: run all tests
Steps:
    1. get Test list
    2. iterate through configurations (read from folders)
    3. start TestSetupInstance for each with list of tests (optional parallel?)
    4. collect TestResults
    5. print testResults
*/
public class TestManager {
    private static final Logger LOGGER = LogManager.getLogger();
    Callable testsCreator;
    List<TestSetupInstance> setups;

    public TestManager(Callable testCreator, Path setupsPath){
        this.testsCreator = testCreator;

        // read in setupsFolder
        try {
            this._collectSetups(setupsPath.toFile());
        } catch (Exception ignored) {}
    }

    /**
     * Fills 'TestManager.setups' with the docker setups in the given folder.
     * Will only look in subfolders named 'apache', 'caddy' or 'nginx'.
     * @param setupsFolder the folder to search for setups
     */
    private void _collectSetups(File setupsFolder) throws Exception {
        assert setupsFolder.isDirectory();
        int port = 11443;
        var setups = new ArrayList<TestSetupInstance>();

        List<String> folderNames = List.of(
                "apache",
                "caddy",
                "nginx"
        ); //the subfolders to search for setups in
        boolean disableCertA = false; //flag to disable the inclusion of CertA setups

        for (File elem : Objects.requireNonNull(setupsFolder.listFiles())) {
            if (!elem.isDirectory()) {continue;}
            if (folderNames.contains(elem.getName())) {
                for (File setup : Objects.requireNonNull(elem.listFiles())) {
                    if (!setup.isDirectory()) {continue;}
                    String name = setup.getName();
                    if (name.startsWith(".")) {continue;}
                    if (name.endsWith("certA") && disableCertA) {continue;}
                    switch (name) {
                        case "domains":
                            setups.add(new TestSetupInstance(
                                    port,
                                    (List<BaseTestCase>) this.testsCreator.call(),
                                    setup.toPath(),
                                    "siteA.org",
                                    "siteB.org",
                                    false,
                                    true
                                )
                            );
                            port++; //next test should get next port
                            break;
                        case "domains_certA":
                            setups.add(new TestSetupInstance(
                                    port,
                                    (List<BaseTestCase>) this.testsCreator.call(),
                                    setup.toPath(),
                                    "siteA.org",
                                    "siteB.org",
                                    true,
                                    true
                                )
                            );
                            port++; //next test should get next port
                            break;
                        case "subdomains":
                            setups.add(new TestSetupInstance(
                                    port,
                                    (List<BaseTestCase>) this.testsCreator.call(),
                                    setup.toPath(),
                                    "siteA.site.org",
                                    "siteB.site.org",
                                    false,
                                    true
                                )
                            );
                            port++; //next test should get next port
                            break;
                        case "subdomains_certA":
                            setups.add(new TestSetupInstance(
                                    port,
                                    (List<BaseTestCase>) this.testsCreator.call(),
                                    setup.toPath(),
                                    "siteA.site.org",
                                    "siteB.site.org",
                                    true,
                                    true
                                )
                            );
                            port++; //next test should get next port
                            break;
                        case "open":
                            setups.add(new TestSetupInstance(
                                    port,
                                    (List<BaseTestCase>) this.testsCreator.call(),
                                    setup.toPath(),
                                    "siteA.org",
                                    "siteB.org",
                                    false,
                                    false
                                )
                            );
                            port++; //next test should get next port
                            break;
                        default:
                            LOGGER.warn("unknown configuration in {}: {}", elem, setup.getName());
                            break;
                    }
                }

            }
        }
        this.setups = setups;
    }

    /**
     * starts the test runs for all TestSetupInstances
     */
    public void run() {
        for (TestSetupInstance setup : this.setups) {
            setup.runTests();
        }
    }

    /**
     * same as run, but in parallel ;)
     */
    public void runParallel() {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (TestSetupInstance setup : this.setups) {
            executor.execute(setup::runTests);
        }
        try {
            Thread.sleep(100L);
            executor.shutdown();

            boolean finishedInTime = executor.awaitTermination(500, TimeUnit.SECONDS);
            if (!finishedInTime) {
                LOGGER.error("executor timeout reached");
            }
        } catch (InterruptedException ignored) {}
    }

    /**
     * Collects the results of the test setups.
     * Makes most sense to run after TestManager.run() has finished
     * @return an ArrayList of all TestSetupResults
     */
    public List<TestSetupResult> getResults() {
        var results = new ArrayList<TestSetupResult>();
        for (TestSetupInstance setup : this.setups) {
            results.add(setup.getResults());
        }
        return  results;
    }
}
