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
        tests.add(new Connect_A_AA("A -> AA (reconnect to A)", ProtocolVersion.TLS12));
        tests.add(new Connect_A_AA("A -> AA (reconnect to A) TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_A_AB("A -> AB", ProtocolVersion.TLS12));
        tests.add(new Connect_A_AB("A -> AB TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_A_BA("A -> BA", ProtocolVersion.TLS12));
        tests.add(new Connect_A_BA("A -> BA TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_A_BB("A -> BB (Base Test Case)", ProtocolVersion.TLS12));
        tests.add(new Connect_A_BB("A -> BB (Base Test Case) TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_A_nA("A -> nA", ProtocolVersion.TLS12));
        tests.add(new Connect_A_nA("A -> nA TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_A_nB("A -> nB", ProtocolVersion.TLS12));
        tests.add(new Connect_A_nB("A -> nB TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_A_nX("A -> nX", ProtocolVersion.TLS12));
        tests.add(new Connect_A_nX("A -> nX TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_A_XX("A -> XX", ProtocolVersion.TLS12));
        tests.add(new Connect_A_XX("A -> XX TLS 1.3", ProtocolVersion.TLS13));

        tests.add(new Connect_B_BB_noCert("B -> BB (no cert)", ProtocolVersion.TLS12));
        tests.add(new Connect_B_BB_noCert("B -> BB (no cert) TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_B_BB_CertA("B -> BB (with certA)", ProtocolVersion.TLS12));
        tests.add(new Connect_B_BB_CertA("B -> BB (with certA) TLS 1.3", ProtocolVersion.TLS13));

        /*  these tests have access to a client certificate for site B
            we do this just for reference, the attacker wouldn't have this ability in our attack scenario
            these cases are labeled as '_test' for this reason
        */
        tests.add(new Connect_B_AA_test("[D] B -> AA (baseCase reversed)", ProtocolVersion.TLS12));
        tests.add(new Connect_B_AA_test("[D] B -> AA (baseCase reversed) TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_B_BB_test("[D] B -> BB (reconnect to B)", ProtocolVersion.TLS12));
        tests.add(new Connect_B_BB_test("[D] B -> BB (reconnect to B) TLS 1.3", ProtocolVersion.TLS13));
        tests.add(new Connect_B_XX_test("[D] B -> XX", ProtocolVersion.TLS12));
        tests.add(new Connect_B_XX_test("[D] B -> XX TLS 1.3", ProtocolVersion.TLS13));

        return tests;
    }
}
