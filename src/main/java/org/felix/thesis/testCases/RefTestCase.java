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

public abstract class RefTestCase {
    private static final Logger LOGGER = LogManager.getLogger("TestCase");

    int port;
    final String name;
    String siteADomain;
    String siteBDomain;
    Path siteAClientCert;
    Path siteBClientCert;
    ProtocolVersion version;
    boolean siteANeedsCert;
    boolean siteBNeedsCert;
    public TestOutcome[] expectedTestOutcome;

    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site B and attaches the session Ticket to the request
     * </br>
     * Note: this test case always fails for setups that require a certificate on siteA
     */
    public RefTestCase(String name, ProtocolVersion version) {
        this.name = name;
        this.version = version;
    }

    public void setup(int port, boolean siteANeedsCert, boolean siteBNeedsCert, String siteADomain, String siteBDomain, Path siteAClientCert, Path siteBClientCert) {
        this.port = port;
        this.siteANeedsCert = siteANeedsCert;
        this.siteBNeedsCert = siteBNeedsCert;
        this.siteADomain = siteADomain;
        this.siteBDomain = siteBDomain;
        this.siteAClientCert = siteAClientCert;
        this.siteBClientCert = siteBClientCert;
    }
    public String getName() {
        return name;
    }

    /**
     * Add the certificate to the connection configuration.
     * @param config the config to apply the certificate to
     * @param certPath the path to the cert (on disk)
     * @return the modified config
     */
    Config applyCert(Config config, Path certPath) {
        config.setClientAuthentication(true);
        config.setAutoSelectCertificate(false);
        config.setClientAuthenticationType(ClientAuthenticationType.CERTIFICATE_BASED);
        config.setClientCertificateTypes(ClientCertificateType.RSA_SIGN); // is this the appropriate type?

        try {
            //make sure the file exists and contains relevant stuff
            BufferedReader certReader = Files.newBufferedReader(certPath);
            certReader.mark(1024); //set mark at the beginning
            assert Objects.equals(certReader.readLine(), "-----BEGIN CERTIFICATE-----");
            certReader.reset(); //jump back to mark

            //read private key and client cert
            //the pem file contains both the client cert and the client key
            PEMParser pemParser = new PEMParser(certReader);

            // read the first pemObject from the file
            Object pemObjectCert = pemParser.readObject();
            X509CertificateHolder certHolder = (X509CertificateHolder) pemObjectCert;
            X509Certificate x509cert = new JcaX509CertificateConverter().getCertificate(certHolder);
            org.bouncycastle.asn1.x509.Certificate bcCert = org.bouncycastle.asn1.x509.Certificate.getInstance(x509cert.getEncoded());

            @SuppressWarnings("deprecation") Certificate tlsCert = new Certificate(new org.bouncycastle.asn1.x509.Certificate[]{bcCert}); //this is great (old)...

            // read the second pemObject from the file
            PemObject pemObjKey = pemParser.readPemObject();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(pemObjKey.getContent());
            PrivateKey pk = KeyFactory.getInstance("RSA").generatePrivate(privKeySpec);

            //Certificate cert, PrivateKey key
            CertificateKeyPair keyPair = new CertificateKeyPair(tlsCert, pk);
            config.setDefaultExplicitCertificateKeyPair(keyPair);
        } catch (IOException e) {
            LOGGER.error("! UNABLE READ CLIENT CERT AND/OR KEY");
            LOGGER.error(e);
        } catch (InvalidKeySpecException e) {
            LOGGER.error("! UNABLE DECODE CLIENT KEY");
            LOGGER.error(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); //this should not happen...
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
        return config;
    }

    /**
     * builds the State(config + workflow trace) for this test run
     * @return the State object
     */
    public State getStateA() {
        Config config = BaseConfigCreator.buildConfig(port, siteADomain, version);
        if (this.siteANeedsCert) config = applyCert(config, siteAClientCert);
        WorkflowTrace trace = BaseWorkflowCreator.getNormalWorkflowTrace(config);
        return new State(config, trace);
    }

    /**
     * builds the State(config + workflow trace) for this test run
     * @param ticket the session Ticket to use for the resumption
     * @return the State object
     */
    public State getStateB(Ticket ticket) {
        Config config = BaseConfigCreator.buildConfig(port, siteBDomain, version);
        ticket.applyTo(config);
        WorkflowTrace trace = BaseWorkflowCreator.getResumptionWorkflowTrace(config, siteBDomain);
        return new State(config, trace);
    }
}
