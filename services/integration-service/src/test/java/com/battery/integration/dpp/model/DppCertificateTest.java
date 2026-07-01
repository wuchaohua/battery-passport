package com.battery.integration.dpp.model;

import com.battery.integration.dpp.model.DppCertificate.CertStatus;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DppCertificateTest {

    @Test
    void testDefaultConstructor() {
        DppCertificate c = new DppCertificate();
        assertNull(c.getCertId());
        assertNull(c.getStatus());
    }

    @Test
    void testActiveCertificateLifecycle() {
        DppCertificate c = new DppCertificate();
        c.setCertId("CERT-EU-001");
        c.setEnterpriseId("ENT-001");
        c.setSubjectDn("CN=ENT-001, O=Trina Solar, C=CN, OU=BatteryPassport");
        c.setIssuerDn("CN=ENT-001, O=Trina Solar, C=CN, OU=BatteryPassport");
        c.setSerialNumber("A1B2C3D4E5F67890");
        c.setSha256Thumbprint("abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
        c.setStatus(CertStatus.ACTIVE);
        c.setIssuedAt(LocalDateTime.of(2026, 6, 1, 0, 0));
        c.setExpiresAt(LocalDateTime.of(2027, 6, 1, 0, 0));

        assertEquals("CERT-EU-001", c.getCertId());
        assertEquals("ENT-001", c.getEnterpriseId());
        assertTrue(c.getSubjectDn().contains("Trina Solar"));
        assertEquals("A1B2C3D4E5F67890", c.getSerialNumber());
        assertEquals(CertStatus.ACTIVE, c.getStatus());
        assertNotNull(c.getIssuedAt());
        assertNotNull(c.getExpiresAt());
    }

    @Test
    void testCertificateRevocation() {
        DppCertificate c = new DppCertificate();
        c.setCertId("CERT-EU-002");
        c.setStatus(CertStatus.ACTIVE);
        assertNull(c.getRevokedAt());

        c.setStatus(CertStatus.REVOKED);
        c.setRevokedAt(LocalDateTime.now());
        c.setRevocationReason("Private key compromised");

        assertEquals(CertStatus.REVOKED, c.getStatus());
        assertNotNull(c.getRevokedAt());
        assertEquals("Private key compromised", c.getRevocationReason());
    }

    @Test
    void testCertificateExpiry() {
        DppCertificate c = new DppCertificate();
        c.setExpiresAt(LocalDateTime.of(2026, 7, 1, 0, 0));
        c.setStatus(CertStatus.EXPIRED);
        assertEquals(CertStatus.EXPIRED, c.getStatus());
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0), c.getExpiresAt());
    }

    @Test
    void testAllCertStatuses() {
        assertNotNull(CertStatus.valueOf("ACTIVE"));
        assertNotNull(CertStatus.valueOf("EXPIRED"));
        assertNotNull(CertStatus.valueOf("REVOKED"));
    }

    @Test
    void testX509CertificateTransient() {
        DppCertificate c = new DppCertificate();
        assertNull(c.getX509Certificate());
    }
}
