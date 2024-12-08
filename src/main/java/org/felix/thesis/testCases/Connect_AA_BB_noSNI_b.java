package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.certificate.CertificateKeyPair;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.constants.ClientAuthenticationType;
import de.rub.nds.tlsattacker.core.constants.ClientCertificateType;
import de.rub.nds.tlsattacker.core.constants.ProtocolVersion;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.BaseWorkflowCreator;
import org.felix.thesis.TestOutcome;
import org.felix.thesis.sessionTickets.Ticket;
import org.xbill.DNS.KEYRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Objects;

public class Connect_AA_BB_noSNI_b extends Connect_AA_BB {
    public Connect_AA_BB_noSNI_b(String name, ProtocolVersion version) {
        super(name, version);
        this.expectedTestOutcome = new TestOutcome[] {
                TestOutcome.secondRequest_tlsAlert_unexpectedMessage,
                TestOutcome.secondRequest_http421_misdirectedRequest
        };
    }

    //getStateA is the same as for the base case

    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain, false, version); //no SNI
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }
}