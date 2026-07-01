package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.audit.AuditService;
import com.battery.integration.dpp.audit.AuditService.ChainVerification;
import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.model.AuditEvidence;
import com.battery.integration.dpp.model.AuditEvidence.EventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class DppAuditControllerTest {

    private DppAuditController controller;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        DppRegistryProperties props = new DppRegistryProperties();
        auditService = new AuditService(props);
        controller = new DppAuditController(auditService);

        // Seed some audit data
        auditService.record("ENT-AUDIT-1", EventType.ENTERPRISE_REGISTERED,
                "admin", "Registration 1", "digest1");
        auditService.record("ENT-AUDIT-1", EventType.CERTIFICATE_ISSUED,
                "admin", "Cert issued 1", "digest2");
        auditService.record("ENT-AUDIT-2", EventType.ENTERPRISE_REGISTERED,
                "admin", "Registration 2", "digest3");
    }

    @Test
    void testGetAuditChain() {
        Result<List<AuditEvidence>> result = controller.getAuditChain("ENT-AUDIT-1");
        assertEquals(200, result.getCode());
        assertEquals(2, result.getData().size());
    }

    @Test
    void testGetAuditChainEmpty() {
        Result<List<AuditEvidence>> result = controller.getAuditChain("NONEXISTENT");
        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    @Test
    void testVerifyAuditChain() {
        Result<ChainVerification> result = controller.verifyAuditChain("ENT-AUDIT-1");
        assertEquals(200, result.getCode());
        assertTrue(result.getData().valid());
        assertTrue(result.getData().discrepancies().isEmpty());
    }

    @Test
    void testVerifyAuditChainEmpty() {
        Result<ChainVerification> result = controller.verifyAuditChain("NONEXISTENT");
        assertEquals(200, result.getCode());
        assertTrue(result.getData().valid());
    }

    @Test
    void testGetTrackedEnterprises() {
        Result<Set<String>> result = controller.getTrackedEnterprises();
        assertEquals(200, result.getCode());
        assertTrue(result.getData().contains("ENT-AUDIT-1"));
        assertTrue(result.getData().contains("ENT-AUDIT-2"));
        assertEquals(2, result.getData().size());
    }

    @Test
    void testAuditChainIntegrityAfterMultipleEntries() {
        // Add more entries to create a longer chain
        for (int i = 0; i < 5; i++) {
            auditService.record("ENT-AUDIT-LONG", EventType.ENTERPRISE_UPDATED,
                    "admin", "Update event " + i, "digest-" + i);
        }

        Result<List<AuditEvidence>> chain = controller.getAuditChain("ENT-AUDIT-LONG");
        assertEquals(5, chain.getData().size());

        Result<ChainVerification> verification = controller.verifyAuditChain("ENT-AUDIT-LONG");
        assertTrue(verification.getData().valid());
    }
}
