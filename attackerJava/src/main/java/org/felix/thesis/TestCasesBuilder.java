package org.felix.thesis;

import org.felix.thesis.testCases.*;

import java.util.ArrayList;
import java.util.List;

public class TestCasesBuilder {
    public static List<BaseTestCase> getTestCases() {
        ArrayList<BaseTestCase> tests = new ArrayList<>();
        // base tests
        tests.add(new BaseTestCase("BaseTestCase"));
        tests.add(new BaseTestCaseReversed("baseTestCaseReversed"));
        tests.add(new TestReconnectToA("reconnectToA"));
        tests.add(new TestReconnectToB("reconnectToB"));
        tests.add(new TestReconnectToB_NoCert("reconnectToB - no Cert"));
        tests.add(new ReconnectAInvalidDomain("ReconnectA - InvalidDomain"));
        tests.add(new ReconnectBInvalidDomain("ReconnectB - InvalidDomain"));
        return tests;
    }
}
