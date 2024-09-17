package org.felix.thesis.testCases;

import de.rub.nds.tlsattacker.core.certificate.CertificateKeyPair;
import de.rub.nds.tlsattacker.core.config.Config;
import de.rub.nds.tlsattacker.core.connection.OutboundConnection;
import de.rub.nds.tlsattacker.core.constants.ClientAuthenticationType;
import de.rub.nds.tlsattacker.core.constants.ClientCertificateType;
import de.rub.nds.tlsattacker.core.protocol.message.extension.sni.ServerNamePair;
import de.rub.nds.tlsattacker.core.state.State;
import de.rub.nds.tlsattacker.core.workflow.WorkflowTrace;
import de.rub.nds.tlsattacker.core.workflow.factory.WorkflowTraceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.felix.thesis.BaseConfigCreator;
import org.felix.thesis.sessionTickets.Ticket;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.Objects;

public class BaseTestCase {
    private static final Logger LOGGER = LogManager.getLogger();

    private final String name;
    boolean sendsCorrectCertToA;
    boolean sendsCorrectCertToB;
    boolean doesSomethingIllegal;

    /**
     * 1. connects to site A and requests a session Ticket
     * </br>
     * 2. connects to site B and attaches the session Ticket to the request
     * </br>
     * Note: this test case always fails for setups that require a certificate on siteA
     */
    public BaseTestCase(String name) {
        this.name = name;
        this.doesSomethingIllegal = true;
        this.sendsCorrectCertToA = false;
        this.sendsCorrectCertToB = false;
    }

    public String getName() {
        return name;
    }

    /**
     * return true if we: </br>
     *    1. do something illegal or </br>
     *    2. siteA needs the correct cert, and we don't send it or </br>
     *    3. siteB needs the correct cert, and we don't send it
     * @return whether the test is expected to go smoothly or have the server fail
     */
    public boolean getExpectedToFail(boolean siteANeedsClientCert, boolean siteBNeedsClientCert) {
        return this.doesSomethingIllegal
                || siteANeedsClientCert && !this.sendsCorrectCertToA
                || siteBNeedsClientCert && !this.sendsCorrectCertToB;
    }

    /**
     * prepare the config
     * @param port the port to connect on
     * @param domain the domain to set in the SNI extension
     * @return the configured config
     */
    private Config buildConfig(int port, String domain) {
        Config config = BaseConfigCreator.getConfig();

        // set port
        config.setDefaultClientConnection(new OutboundConnection(port));

        // add SNI extension
        config.setAddServerNameIndicationExtension(true);
        ServerNamePair sn = new ServerNamePair(
                (byte) 0,
                domain.getBytes(StandardCharsets.US_ASCII)
        );
        config.setDefaultSniHostnames(List.of(sn));
        return config;
    }

    /**
     * Add the certificate to the connection configuration.
     * @param config the config to apply the certificate to
     * @param certPath the path to the cert (on disk)
     * @return the modified config
     */
    Config applyCert(Config config, Path certPath) {
        config.setClientAuthentication(true);
        config.setClientAuthenticationType(ClientAuthenticationType.CERTIFICATE_BASED);
        config.setClientCertificateTypes(ClientCertificateType.RSA_SIGN); // is this the appropriate type?

        try {
            //make sure the file exists and contains relevant stuff
            BufferedReader assertReader = Files.newBufferedReader(certPath);
            assert Objects.equals(assertReader.readLine(), "-----BEGIN CERTIFICATE-----");
            assertReader.close();

            //read private key and client cert
            //the pem file contains both the client cert and the client key
            PEMParser pemParser = new PEMParser(Files.newBufferedReader(certPath));

            LOGGER.info("read first pem object");
            // read the first pemObject from the file
            Object pemObjectCert = pemParser.readObject();
            X509CertificateHolder certHolder = (X509CertificateHolder) pemObjectCert;
            X509Certificate x509cert = new JcaX509CertificateConverter().getCertificate(certHolder);

            LOGGER.info("read second pem object");
            // read the second pemObject from the file
            PemObject pemObjKey = pemParser.readPemObject();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(pemObjKey.getContent());
            PrivateKey pk = KeyFactory.getInstance("RSA").generatePrivate(privKeySpec);

            LOGGER.info("read both objects from the file");
            CertificateFactory.getInstance("X.509").generateCertificates(new BufferedInputStream());
            Certificate cert = Certificate.parse(new ByteArrayInputStream(x509cert.getEncoded()));
            CertificateKeyPair keyPair = new CertificateKeyPair(cert, pk);

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
     * @param port the port to contact
     * @param siteADomain the domain of site A
     * @param siteAClientCert the client cert for site A
     * @return the State object
     */
    public State getStateA(int port, String siteADomain, Path siteAClientCert) {
        //basic connection config
        Config config = this.buildConfig(port, siteADomain);

        // return state
        WorkflowTrace trace = BaseConfigCreator.getWorkflowTrace(config);
        return new State(config, trace);
    }

    /**
     * builds the State(config + workflow trace) for this test run
     * @param port the port to contact
     * @param siteBDomain the domain of site A
     * @param siteBClientCert the client cert for site A
     * @param ticket the session Ticket to use for the resumption
     * @return the State object
     */
    public State getStateB(int port, String siteBDomain, Path siteBClientCert, Ticket ticket) {
        // basic connection config
        Config config = this.buildConfig(port, siteBDomain);

        // set workflow type to reconnect
        config.setWorkflowTraceType(WorkflowTraceType.RESUMPTION);
//        config.setWorkflowTraceType(WorkflowTraceType.TLS13_PSK); //is this better??

        // add session ticket
        ticket.applyTo(config);

        // return state
        WorkflowTrace trace = BaseConfigCreator.getWorkflowTrace(config);
        return new State(config, trace);
    }
}
