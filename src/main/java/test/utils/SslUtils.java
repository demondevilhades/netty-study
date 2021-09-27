package test.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.FingerprintTrustManagerFactory;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author awesome
 */
@Slf4j
public class SslUtils {

    public static SslContext readServerSslContextFromFile(File keyCertChainFile, File keyFile) throws SSLException {
        return SslContextBuilder.forServer(keyCertChainFile, keyFile).sslProvider(SslProvider.OPENSSL).build();
    }

    public static SslContext getClientSslContext() throws SSLException {
        return SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).sslProvider(SslProvider.OPENSSL).build();
    }

    public static SslContext getTrustClientSslContext(String algorithm, String fingerprints) throws SSLException {
        return SslContextBuilder.forClient()
                .trustManager(FingerprintTrustManagerFactory.builder(algorithm).fingerprints(fingerprints).build())
                .sslProvider(SslProvider.OPENSSL).build();
    }

    public void test() throws CertificateException {
        SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
        // File certificateFile = selfSignedCertificate.certificate();
        // File privateKeyFile = selfSignedCertificate.privateKey();

        X509Certificate x509Certificate = selfSignedCertificate.cert();
        Principal principal = x509Certificate.getSubjectDN();
        String sigAlgName = x509Certificate.getSigAlgName();

        log.info("Principal = {}", principal);
        log.info("sigAlgName = {}", sigAlgName);
    }

    public void testServerClient() throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance("");
        char[] password = null;
        FileInputStream fis = null;
        keyStore.load(fis, password);

        {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, password);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
        }

        {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            SSLEngine sslEngine = sslContext.createSSLEngine();
            sslEngine.setUseClientMode(true);
        }
    }
}
