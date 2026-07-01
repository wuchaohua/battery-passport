package com.battery.integration.dpp.model;

import com.battery.integration.dpp.model.AuditEvidence.EventType;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AuditEvidenceTest {

    @Test
    void testDefaultConstructor() {
        AuditEvidence e = new AuditEvidence();
        assertNull(e.getEvidenceId());
    }

    @Test
    void testParameterizedConstructor() {
        String previousDigest = "0000000000000000000000000000000000000000000000000000000000000000";
        AuditEvidence e = new AuditEvidence(
                "EVID-001", "ENT-001", EventType.ENTERPRISE_REGISTERED,
                "admin@platform", "Enterprise pre-registered", "a1b2c3d4", previousDigest);
        assertEquals("EVID-001", e.getEvidenceId());
        assertEquals("ENT-001", e.getEnterpriseId());
        assertEquals(EventType.ENTERPRISE_REGISTERED, e.getEventType());
        assertEquals("admin@platform", e.getOperatorId());
        assertEquals("a1b2c3d4", e.getPayloadDigest());
        assertEquals(previousDigest, e.getPreviousDigest());
        assertNotNull(e.getOccurredAt());
        assertNull(e.getSignatureValue());
        assertNull(e.getSigningCertThumbprint());
    }

    @Test
    void testAllEventTypes() {
        assertNotNull(EventType.valueOf("ENTERPRISE_REGISTERED"));
        assertNotNull(EventType.valueOf("ENTERPRISE_UPDATED"));
        assertNotNull(EventType.valueOf("CERTIFICATE_ISSUED"));
        assertNotNull(EventType.valueOf("CERTIFICATE_BOUND"));
        assertNotNull(EventType.valueOf("CERTIFICATE_REVOKED"));
        assertNotNull(EventType.valueOf("REGISTRATION_SUBMITTED"));
        assertNotNull(EventType.valueOf("REGISTRATION_CONFIRMED"));
        assertNotNull(EventType.valueOf("REGISTRATION_REJECTED"));
        assertNotNull(EventType.valueOf("REGISTRATION_REVOKED"));
        assertNotNull(EventType.valueOf("AUDIT_CHAIN_VERIFIED"));
    }

    @Test
    void testSignatureFields() {
        AuditEvidence e = new AuditEvidence();
        e.setSignatureValue("MEUCIQD...");
        e.setSigningCertThumbprint("abcdef123456");
        e.setOccurredAt(LocalDateTime.of(2026, 6, 15, 10, 30));

        assertEquals("MEUCIQD...", e.getSignatureValue());
        assertEquals("abcdef123456", e.getSigningCertThumbprint());
        assertEquals(LocalDateTime.of(2026, 6, 15, 10, 30), e.getOccurredAt());
    }

    @Test
    void testChainLinkReference() {
        AuditEvidence e = new AuditEvidence();
        e.setPreviousDigest("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6");
        assertEquals("a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6", e.getPreviousDigest());
    }
}
