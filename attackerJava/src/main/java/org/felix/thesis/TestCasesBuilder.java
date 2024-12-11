package org.felix.thesis;

import org.felix.thesis.testCases.*;

import java.util.ArrayList;
import java.util.List;

public class TestCasesBuilder {
    public static List<RefTestCase> getTestCases() {
        ArrayList<RefTestCase> tests = new ArrayList<>();
        /*
        * the naming follows the sites referenced in the SNI extension and Host header of the two connections
        * Connect_AB_BB means the following:
        *   first request:
        *      SNIExtension = siteA
        *      HostHeader   = siteB
        *   second request:
        *      SNIExtension = siteB
        *      HostHeader   = siteB
        * we mostly only focus on tests where the second request mentions siteB.
         */


        /*just as a test, we want site B, so we should ask for site B somewhere*/
        tests.add(new Connect_A_AA("A -> AA (reconnect to A)"));
        tests.add(new Connect_A_AB("A -> AB"));
        tests.add(new Connect_A_BA("A -> BA"));
        tests.add(new Connect_A_BB("A -> BB (Base Test Case)"));
        tests.add(new Connect_A_nB("A -> nB"));
        tests.add(new Connect_A_XX("A -> XX"));

        tests.add(new Connect_B_BB("B -> BB (no cert)"));
        tests.add(new Connect_B_BB_CertA("B -> BB (with certA)"));

        /*  these tests have access to a client certificate for site B
            we do this just for reference, the attacker wouldn't have this ability in our attack scenario
            these cases are labeled as '_test' for this reason
        */
        tests.add(new Connect_B_AA_test("B -> AA (baseCase reversed)"));
        tests.add(new Connect_B_BB_test("B -> BB (reconnect to B)"));
        tests.add(new Connect_B_XX_test("B -> XX"));

        return tests;
    }
}
