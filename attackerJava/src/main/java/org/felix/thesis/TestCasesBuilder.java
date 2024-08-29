package org.felix.thesis;

import java.util.ArrayList;
import java.util.List;

public class TestCasesBuilder {
    public static List<TestCase> getTestCases() {
        ArrayList<TestCase> tests = new ArrayList<>();
        tests.add(buildBaseTest());
        tests.add(buildBaseTest());
        tests.add(buildBaseTest());
        tests.add(buildBaseTest());
        tests.add(buildBaseTest());
        tests.add(buildBaseTest());
        tests.add(buildBaseTest());
        tests.add(buildBaseTest());
        return tests;
    }


    private static TestCase buildBaseTest() {
        return new TestCase();
    }
}
