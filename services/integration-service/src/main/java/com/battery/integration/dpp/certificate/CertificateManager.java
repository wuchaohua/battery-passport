package com.battery.integration.dpp.certificate;

import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.DppCertificate;
import com.battery.integration.dpp.model.DppCertificate.CertStatus;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class CertificateManager {

    private static final Logger log = LoggerFactory.getLogger(CertificateManager.class);
    private static final String KEY_STORE_TYPE = "PKCS12";
    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 4096;

    private final DppRegistryProperties properties;
    private final Map<String, DppCertificate> certificateStore = new HashMap<>();

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CertificateManager(DppRegistryProperties properties) {
        this.properties = properties;
    }

    public DppCertificate generateEnterpriseCertificate(String enterpriseId,
                                                         String legalName,
                                                         String countryCode) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyGen.initialize(KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyGen.generateKeyPair();

        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        cal.add(Calendar.YEAR, 1);
        Date expiry = cal.getTime();

        // Build properly escaped X500Name using Bouncy Castle builder
        X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
        nameBuilder.addRDN(BCStyle.CN, enterpriseId);
        nameBuilder.addRDN(BCStyle.O, legalName);
        nameBuilder.addRDN(BCStyle.C, countryCode);
        nameBuilder.addRDN(BCStyle.OU, "BatteryPassport");
        X500Name subjectName = nameBuilder.build();

        DppCertificate dppCert = new DppCertificate();
        dppCert.setCertId(UUID.randomUUID().toString());
        dppCert.setEnterpriseId(enterpriseId);
        dppCert.setSubjectDn(subjectName.toString());
        dppCert.setIssuerDn(subjectName.toString());
        dppCert.setSerialNumber(UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        dppCert.setSha256Thumbprint(computeThumbprint(keyPair.getPublic().getEncoded()));
        dppCert.setStatus(CertStatus.ACTIVE);
        dppCert.setIssuedAt(LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()));
        dppCert.setExpiresAt(LocalDateTime.ofInstant(expiry.toInstant(), ZoneId.systemDefault()));

        storeKeyPair(enterpriseId, subjectName, keyPair);

        certificateStore.put(dppCert.getCertId(), dppCert);
        log.info("Generated certificate {} for enterprise {}", dppCert.getCertId(), enterpriseId);
        return dppCert;
    }

    public KeyManager[] getKeyManagers(String enterpriseId) throws Exception {
        Path ksPath = Paths.get(properties.getKeyStoreDir(), enterpriseId + ".p12");
        if (!Files.exists(ksPath)) {
            throw new IllegalStateException("Keystore not found for enterprise: " + enterpriseId);
        }
        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        try (FileInputStream fis = new FileInputStream(ksPath.toFile())) {
            ks.load(fis, properties.getKeyStorePassword().toCharArray());
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, properties.getKeyStorePassword().toCharArray());
        return kmf.getKeyManagers();
    }

    public TrustManager[] getTrustManagers() throws Exception {
        String trustStorePath = properties.getTrustStorePath();
        if (trustStorePath == null || trustStorePath.isEmpty()) {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null);
            return tmf.getTrustManagers();
        }
        KeyStore ts = KeyStore.getInstance(KEY_STORE_TYPE);
        try (FileInputStream fis = new FileInputStream(trustStorePath)) {
            ts.load(fis, properties.getTrustStorePassword().toCharArray());
        }
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ts);
        return tmf.getTrustManagers();
    }

    public DppCertificate getCertificate(String certId) {
        return certificateStore.get(certId);
    }

    public List<DppCertificate> getCertificatesForEnterprise(String enterpriseId) {
        return certificateStore.values().stream()
                .filter(c -> enterpriseId.equals(c.getEnterpriseId()))
                .toList();
    }

    public void revokeCertificate(String certId, String reason) {
        DppCertificate cert = certificateStore.get(certId);
        if (cert != null) {
            cert.setStatus(CertStatus.REVOKED);
            cert.setRevokedAt(LocalDateTime.now());
            cert.setRevocationReason(reason);
            log.info("Revoked certificate {}: {}", certId, reason);
        }
    }

    private void storeKeyPair(String enterpriseId, X500Name subjectName, KeyPair keyPair) throws Exception {
        Path ksDir = Paths.get(properties.getKeyStoreDir());
        Files.createDirectories(ksDir);

        KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE);
        ks.load(null, properties.getKeyStorePassword().toCharArray());

        long now = System.currentTimeMillis();
        Date firstDate = new Date(now);
        Date lastDate = new Date(now + 365L * 24 * 60 * 60 * 1000);

        java.math.BigInteger serial = new java.math.BigInteger(64, new java.security.SecureRandom());

        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                subjectName, serial, firstDate, lastDate, subjectName, keyPair.getPublic());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);
        java.security.cert.X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        ks.setKeyEntry(enterpriseId, keyPair.getPrivate(),
                properties.getKeyStorePassword().toCharArray(),
                new Certificate[]{cert});

        Path ksPath = ksDir.resolve(enterpriseId + ".p12");
        try (FileOutputStream fos = new FileOutputStream(ksPath.toFile())) {
            ks.store(fos, properties.getKeyStorePassword().toCharArray());
        }
    }

    private String computeThumbprint(byte[] encoded) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(encoded);
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
