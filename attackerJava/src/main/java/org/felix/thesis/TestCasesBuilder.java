package org.felix.thesis;

import org.felix.thesis.testCases.*;

import java.util.ArrayList;
import java.util.List;

public class TestCasesBuilder {
    public static List<BaseTestCase> getTestCases() {
        ArrayList<BaseTestCase> tests = new ArrayList<>();
        // base tests
        tests.add(new BaseTestCase("BaseTestCase"));
//        tests.add(new BaseCertATestCase("BaseCertATestCase"));
        tests.add(new InvalidDomainBTestCase("InvalidDomainBTestCase"));
//        tests.add(new BothCertsTestCase("BothCertsTestCase"));
        tests.add(new ConnectATwiceTestCase("ConnectATwiceTestCase"));
//        tests.add(new ConnectATwiceCertTestCase("ConnectATwiceCertTestCase"));
        tests.add(new ReconnectToATestCase("ReconnectToATestCase"));
//        tests.add(new ReconnectToACertTestCase("ReconnectToACertTestCase"));
        tests.add(new ConnectBoth("ConnectBoth"));
//        tests.add(new ConnectBothCertA("ConnectBothCertA"));
        return tests;
    }
}
