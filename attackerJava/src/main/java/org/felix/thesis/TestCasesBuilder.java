package org.felix.thesis;

import org.felix.thesis.testCases.*;

import java.util.ArrayList;
import java.util.List;

public class TestCasesBuilder {
    public static List<BaseTestCase> getTestCases() {
        ArrayList<BaseTestCase> tests = new ArrayList<>();
        // base tests
//        tests.add(new BaseTestCase("baseTest"));
//        tests.add(new BaseCertATestCase("certATest"));
//        tests.add(new BothCertsTestCase("bothCerts"));
        tests.add(new ConnectATwiceTestCase("connectATwice"));
        tests.add(new ReconnectToATestCase("reconnectOnA"));
//        tests.add(new ReconnectToACertTestCase("reconnectOnACert"));
        return tests;
    }
}
