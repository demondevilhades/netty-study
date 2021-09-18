package test.utils;

import java.io.File;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SslUtils {

    public void readFromFile() throws SSLException {
        File keyCertChainFile = new File(ResourcesUtils.getResourceFile("ssl/server.crt"));
        File keyFile = new File(ResourcesUtils.getResourceFile("ssl/server_pkcs8.key"));
        SslContext sslCtx = SslContextBuilder.forServer(keyCertChainFile, keyFile)
                .trustManager(InsecureTrustManagerFactory.INSTANCE).sslProvider(SslProvider.OPENSSL).build();
        String[] keys = { "Country Name", "/C", "L", "O", "CN" };
        for (String key : keys) {
            log.info("{} = {}", key, sslCtx.attributes().attr(AttributeKey.<String>valueOf(key)).get());
        }
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

    public static void main(String[] args) throws Exception {
        new SslUtils().readFromFile();
        // new SslUtils().test();
    }
}
