package com.battery.integration.dpp.audit;

import com.battery.integration.dpp.audit.AuditService.ChainVerification;
import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.AuditEvidence;
import com.battery.integration.dpp.model.AuditEvidence.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.security.MessageDigest;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        DppRegistryProperties props = new DppRegistryProperties();
        props.setMaxAuditEntriesPerEnterprise(100);
        auditService = new AuditService(props);
    }

    @Test
    void testRecordSingleEntry() {
        AuditEvidence evidence = auditService.record(
                "ENT-001", EventType.ENTERPRISE_REGISTERED,
                "admin", "Enterprise registered", digest("test-data"));

        assertNotNull(evidence.getEvidenceId());
        assertEquals("ENT-001", evidence.getEnterpriseId());
        assertEquals(EventType.ENTERPRISE_REGISTERED, evidence.getEventType());
        assertEquals("admin", evidence.getOperatorId());
        assertEquals("0", evidence.getPreviousDigest()); // genesis
        assertNotNull(evidence.getOccurredAt());
    }

    @Test
    void testChainWithMultipleEntries() {
        String prevId = null;
        for (int i = 0; i < 5; i++) {
            AuditEvidence evidence = auditService.record(
                    "ENT-001", EventType.CERTIFICATE_ISSUED,
                    "admin", "Event " + i, digest("data-" + i));
            if (i == 0) {
                assertEquals("0", evidence.getPreviousDigest());
            } else {
                assertNotEquals("0", evidence.getPreviousDigest());
            }
            prevId = evidence.getEvidenceId();
        }

        List<AuditEvidence> chain = auditService.getChain("ENT-001");
        assertEquals(5, chain.size());
    }

    @Test
    void testChainVerificationValid() {
        auditService.record("ENT-001", EventType.ENTERPRISE_REGISTERED,
                "admin", "Enterprise registered", digest("reg"));
        auditService.record("ENT-001", EventType.CERTIFICATE_ISSUED,
                "admin", "Cert issued", digest("cert"));
        auditService.record("ENT-001", EventType.CERTIFICATE_BOUND,
                "admin", "Cert bound", digest("bind"));

        ChainVerification result = auditService.verifyChain("ENT-001");
        assertTrue(result.valid());
        assertTrue(result.discrepancies().isEmpty());
    }

    @Test
    void testChainVerificationEmpty() {
        ChainVerification result = auditService.verifyChain("NONEXISTENT");
        assertTrue(result.valid());
        assertTrue(result.discrepancies().isEmpty());
    }

    @Test
    void testChainVerificationTamperedLink() {
        auditService.record("ENT-001", EventType.ENTERPRISE_REGISTERED,
                "admin", "First event", digest("first"));
        auditService.record("ENT-001", EventType.CERTIFICATE_ISSUED,
                "admin", "Second event", digest("second"));

        // Tamper with the chain by modifying the previous digest of the second entry
        List<AuditEvidence> chain = auditService.getChain("ENT-001");
        AuditEvidence tampered = chain.get(1);
        tampered.setPreviousDigest("tampered-hash");

        ChainVerification result = auditService.verifyChain("ENT-001");
        assertFalse(result.valid());
        assertEquals(1, result.discrepancies().size());
        assertTrue(result.discrepancies().get(0).contains("link broken"));
    }

    @Test
    void testMultipleEnterprises() {
        auditService.record("ENT-001", EventType.ENTERPRISE_REGISTERED, "admin", "Reg 1", "digest1");
        auditService.record("ENT-002", EventType.ENTERPRISE_REGISTERED, "admin", "Reg 2", "digest2");

        assertEquals(1, auditService.getChain("ENT-001").size());
        assertEquals(1, auditService.getChain("ENT-002").size());
        assertTrue(auditService.getTrackedEnterprises().contains("ENT-001"));
        assertTrue(auditService.getTrackedEnterprises().contains("ENT-002"));
        assertEquals(2, auditService.getTrackedEnterprises().size());
    }

    @Test
    void testMaxEntriesEnforcement() {
        DppRegistryProperties props = new DppRegistryProperties();
        props.setMaxAuditEntriesPerEnterprise(3);
        AuditService limitedService = new AuditService(props);

        for (int i = 0; i < 5; i++) {
            limitedService.record("ENT-001", EventType.ENTERPRISE_UPDATED,
                    "admin", "Event " + i, digest("data-" + i));
        }

        List<AuditEvidence> chain = limitedService.getChain("ENT-001");
        assertEquals(3, chain.size()); // limited to max 3
        // The oldest entries should have been removed
        assertFalse(chain.get(0).getDescription().contains("Event 0"));
        assertFalse(chain.get(0).getDescription().contains("Event 1"));
    }

    @Test
    void testGetChainNonexistent() {
        List<AuditEvidence> chain = auditService.getChain("NONEXISTENT");
        assertTrue(chain.isEmpty());
    }

    private String digest(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
