package org.felix.thesis;

import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
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
//        tests.add(new Connect_AA_AA("AA -> AA (reconnect to A)"));
//        tests.add(new Connect_AA_XX("AA -> XX"));

//        tests.add(new Connect_AA_AB("AA -> AB"));
//        tests.add(new Connect_AA_BA("AA -> BA"));
        tests.add(new Connect_AA_BB("AA -> BB (Base Test Case)", ProtocolVersion.TLS12));
        tests.add(new Connect_AA_BB("AA -> BB (Base Test Case)", ProtocolVersion.TLS13));
//        tests.add(new Connect_AA_BB_noSNI_b("AA -> BB (no SNI for B)"));

//        tests.add(new Connect_AB_AB("AB -> AB"));
//        tests.add(new Connect_AB_BA("AB -> BA"));
//        tests.add(new Connect_AB_BB("AB -> BB"));

        tests.add(new Connect_BA_AB("BA -> AB", ProtocolVersion.TLS12));
        tests.add(new Connect_BA_AB("BA -> AB TLS 1.3", ProtocolVersion.TLS13));
//        tests.add(new Connect_BA_BA("BA -> BA"));
//        tests.add(new Connect_BA_BB("BA -> BB"));

        /*just for reference, the attacker wouldn't have this ability*/
//        tests.add(new Connect_BB_AA("BB -> AA (baseCase reversed)"));
        tests.add(new Connect_BB_BB("BB -> BB (reconnect to B)", ProtocolVersion.TLS12));
        tests.add(new Connect_BB_BB("BB -> BB (reconnect to B) TLS 1.3", ProtocolVersion.TLS13));
//        tests.add(new Connect_BB_XX("BB -> XX"));
//        tests.add(new Connect_BB_BB_CertA("BB -> BB (with certA)"));
        tests.add(new Connect_BB_BB_noCert("BB* -> BB (*but no ClientCert for B)", ProtocolVersion.TLS12));
        tests.add(new Connect_BB_BB_noCert("BB* -> BB (*but no ClientCert for B) TLS 1.3", ProtocolVersion.TLS13));

        return tests;
    }
}
