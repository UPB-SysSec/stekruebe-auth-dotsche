package org.felix.thesis;

import org.felix.thesis.testCases.*;

import java.util.ArrayList;
import java.util.List;

public class TestCasesBuilder {
    public static List<Connect_AA_BB> getTestCases() {
        ArrayList<Connect_AA_BB> tests = new ArrayList<>();
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
        tests.add(new Connect_AA_AA("AA -> AA (reconnect to A)"));
        tests.add(new Connect_AA_XX("AA -> XX"));

        tests.add(new Connect_AA_AB("AA -> AB"));
        tests.add(new Connect_AA_BA("AA -> BA"));
        tests.add(new Connect_AA_BB("AA -> BB (Base Test Case)"));

        tests.add(new Connect_AB_AB("AB -> AB"));
        tests.add(new Connect_AB_BA("AB -> BA"));
        tests.add(new Connect_AB_BB("AB -> BB"));

        tests.add(new Connect_BA_AB("BA -> AB"));
        tests.add(new Connect_BA_BA("BA -> BA"));
        tests.add(new Connect_BA_BB("BA -> BB"));

        /*just for reference, the attacker wouldn't have this ability*/
        tests.add(new Connect_BB_AA("BB -> AA (baseCase reversed)"));
        tests.add(new Connect_BB_BB("BB -> BB (reconnect to B)"));
        tests.add(new Connect_BB_XX("BB -> XX"));
        tests.add(new Connect_BB_BB_noCert("BB* -> BB (*but no ClientCert for B)"));

        return tests;
    }
}
