package org.felix.thesis;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.List;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * add de.rub.nds.tls.attacker:tls-core:5.3.0 to dependencies
     * NOT de.rub.nds.tlsAttacker:TLS-Core:3.4.0 or similar !
     */
    public static void main(String[] args) {

        LOGGER.info("init"); /*if log4j is unable to find "appenders", add src/main/resources to the classpath*/
        Path path = Path.of("../setups");
        TestManager tm = new TestManager(TestCasesBuilder.getTestCases(), path);

        LOGGER.info("running");
        tm.run();
//        tm.runParallel();

        LOGGER.info("Results:");
        List<TestSetupResult> results = tm.getResults();
        for (TestSetupResult res : results) {
            LOGGER.info(res.toString());
        }
    }
}