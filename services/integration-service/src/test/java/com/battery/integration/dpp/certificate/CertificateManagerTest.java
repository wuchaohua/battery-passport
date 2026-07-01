package com.battery.integration.dpp.certificate;

import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.DppCertificate;
import com.battery.integration.dpp.model.DppCertificate.CertStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

class CertificateManagerTest {

    private CertificateManager certificateManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DppRegistryProperties props = new DppRegistryProperties();
        props.setKeyStoreDir(tempDir.toString());
        certificateManager = new CertificateManager(props);
    }

    @Test
    void testGenerateCertificate() throws Exception {
        DppCertificate cert = certificateManager.generateEnterpriseCertificate(
                "ENT-001", "Trina Solar", "CN");

        assertNotNull(cert.getCertId());
        assertEquals("ENT-001", cert.getEnterpriseId());
        assertTrue(cert.getSubjectDn().contains("ENT-001"));
        assertTrue(cert.getSubjectDn().contains("Trina Solar"));
        assertTrue(cert.getSubjectDn().contains("CN"));
        assertNotNull(cert.getSerialNumber());
        assertEquals(16, cert.getSerialNumber().length());
        assertNotNull(cert.getSha256Thumbprint());
        assertEquals(CertStatus.ACTIVE, cert.getStatus());
        assertNotNull(cert.getIssuedAt());
        assertNotNull(cert.getExpiresAt());
        assertTrue(cert.getExpiresAt().isAfter(cert.getIssuedAt()));
    }

    @Test
    void testGetCertificate() throws Exception {
        DppCertificate cert = certificateManager.generateEnterpriseCertificate(
                "ENT-002", "CATL", "CN");

        DppCertificate found = certificateManager.getCertificate(cert.getCertId());
        assertNotNull(found);
        assertEquals(cert.getCertId(), found.getCertId());

        assertNull(certificateManager.getCertificate("NONEXISTENT"));
    }

    @Test
    void testGetCertificatesForEnterprise() throws Exception {
        certificateManager.generateEnterpriseCertificate("ENT-003", "BYD", "CN");
        certificateManager.generateEnterpriseCertificate("ENT-003", "BYD", "CN");

        assertEquals(2, certificateManager.getCertificatesForEnterprise("ENT-003").size());
        assertEquals(0, certificateManager.getCertificatesForEnterprise("NONEXISTENT").size());
    }

    @Test
    void testRevokeCertificate() throws Exception {
        DppCertificate cert = certificateManager.generateEnterpriseCertificate(
                "ENT-004", "Samsung SDI", "KR");

        assertEquals(CertStatus.ACTIVE, cert.getStatus());
        certificateManager.revokeCertificate(cert.getCertId(), "Key compromise");

        DppCertificate revoked = certificateManager.getCertificate(cert.getCertId());
        assertEquals(CertStatus.REVOKED, revoked.getStatus());
        assertNotNull(revoked.getRevokedAt());
        assertEquals("Key compromise", revoked.getRevocationReason());
    }

    @Test
    void testRevokeNonexistentCertificate() {
        certificateManager.revokeCertificate("NONEXISTENT", "test");
        // Should not throw
    }

    @Test
    void testMultipleCertificatesDifferentEnterprises() throws Exception {
        certificateManager.generateEnterpriseCertificate("ENT-A", "Company A", "DE");
        certificateManager.generateEnterpriseCertificate("ENT-B", "Company B", "FR");
        certificateManager.generateEnterpriseCertificate("ENT-A", "Company A", "DE");

        assertEquals(2, certificateManager.getCertificatesForEnterprise("ENT-A").size());
        assertEquals(1, certificateManager.getCertificatesForEnterprise("ENT-B").size());
    }

    @Test
    void testCertificateThumbprintIsHex() throws Exception {
        DppCertificate cert = certificateManager.generateEnterpriseCertificate(
                "ENT-T", "Test Corp", "US");

        assertNotNull(cert.getSha256Thumbprint());
        assertTrue(cert.getSha256Thumbprint().matches("^[0-9a-f]+$"),
                "Thumbprint should be hex-encoded: " + cert.getSha256Thumbprint());
    }
}
