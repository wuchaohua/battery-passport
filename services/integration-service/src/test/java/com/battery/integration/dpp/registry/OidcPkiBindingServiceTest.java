package com.battery.integration.dpp.registry;

import com.battery.integration.dpp.audit.AuditService;
import com.battery.integration.dpp.certificate.CertificateManager;
import com.battery.integration.dpp.config.DppRegistryProperties;
import com.battery.integration.dpp.identity.OidcPkiBindingService;
import com.battery.integration.dpp.identity.OidcPkiBindingService.BindingResult;
import com.battery.integration.dpp.model.DppEnterprise.RegistrationStatus;
import com.battery.integration.dpp.model.DppRegistration;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

class OidcPkiBindingServiceTest {

    private OidcPkiBindingService bindingService;
    private DppRegistryService registryService;
    private AuditService auditService;
    private CertificateManager certificateManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        DppRegistryProperties props = new DppRegistryProperties();
        props.setKeyStoreDir(tempDir.toString());

        auditService = new AuditService(props);
        certificateManager = new CertificateManager(props);

        DppRegistryClient mockClient = new DppRegistryClient(props, null) {
            @Override
            public com.fasterxml.jackson.databind.JsonNode registerEnterprise(
                    String enterpriseId, String legalName, String vatNumber,
                    String eoriNumber, String countryCode) {
                ObjectNode result = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
                result.put("registryId", "REG-EU-" + enterpriseId);
                result.put("status", "pending");
                return result;
            }

            @Override
            public com.fasterxml.jackson.databind.JsonNode bindCertificate(
                    String dppRegistryId, String certificatePem) {
                ObjectNode result = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
                result.put("status", "bound");
                return result;
            }
        };

        registryService = new DppRegistryService(props, mockClient, auditService);
        bindingService = new OidcPkiBindingService(certificateManager, registryService, auditService);
    }

    @Test
    void testFullIdentityBindingFlow() throws Exception {
        BindingResult result = bindingService.bindIdentity(
                "ENT-001",
                "Trina Solar Co., Ltd",
                "CN",
                "user@trinasolar.com",
                "https://iam.trinasolar.com",
                "admin"
        );

        assertNotNull(result.certificate());
        assertNotNull(result.registration());

        // Verify certificate
        assertEquals("ENT-001", result.certificate().getEnterpriseId());
        assertTrue(result.certificate().getSubjectDn().contains("Trina Solar"));

        // Verify registration
        assertEquals("ENT-001", result.registration().getEnterpriseId());
        assertEquals("user@trinasolar.com", result.registration().getBoundOidcSubject());
        assertEquals("https://iam.trinasolar.com", result.registration().getOidcIssuer());
        assertNotNull(result.registration().getBoundCertId());
        assertEquals(RegistrationStatus.PENDING, result.registration().getStatus());

        // Verify audit trail
        assertEquals(4, auditService.getChain("ENT-001").size());
        assertTrue(auditService.verifyChain("ENT-001").valid());
    }

    @Test
    void testBindIdentityWithExistingRegistration() throws Exception {
        // First binding
        BindingResult first = bindingService.bindIdentity(
                "ENT-002", "CATL", "CN",
                "admin@catl.com", "https://iam.catl.com", "admin");
        assertNotNull(first.registration().getBoundCertId());

        // Second binding for same enterprise reuses existing registration
        BindingResult second = bindingService.bindIdentity(
                "ENT-002", "CATL", "CN",
                "admin@catl.com", "https://iam.catl.com", "admin");

        assertNotNull(second.registration().getBoundCertId());
        assertNotNull(first.registration().getRegistrationId());
    }

    @Test
    void testVerifyBinding() throws Exception {
        bindingService.bindIdentity(
                "ENT-003", "BYD", "CN",
                "user@byd.com", "https://iam.byd.com", "admin");

        assertTrue(bindingService.verifyBinding("ENT-003", "user@byd.com", "https://iam.byd.com"));
        assertFalse(bindingService.verifyBinding("ENT-003", "wrong@user.com", "https://iam.byd.com"));
        assertFalse(bindingService.verifyBinding("ENT-NONEXISTENT", "user", "issuer"));
    }

    @Test
    void testBindIdentityThrowsOnMissingOidcSubject() {
        assertThrows(IllegalArgumentException.class, () ->
            bindingService.bindIdentity("ENT-X", "Test", "DE", null, "https://issuer", "admin"));

        assertThrows(IllegalArgumentException.class, () ->
            bindingService.bindIdentity("ENT-X", "Test", "DE", "", "https://issuer", "admin"));
    }

    @Test
    void testBindIdentityThrowsOnMissingOidcIssuer() {
        assertThrows(IllegalArgumentException.class, () ->
            bindingService.bindIdentity("ENT-X", "Test", "DE", "user", null, "admin"));

        assertThrows(IllegalArgumentException.class, () ->
            bindingService.bindIdentity("ENT-X", "Test", "DE", "user", "", "admin"));
    }

    @Test
    void testAuditChainAfterFullFlow() throws Exception {
        bindingService.bindIdentity("ENT-AUDIT", "Audit Corp", "DE",
                "auditor@corp.com", "https://iam.corp.com", "auditor");

        var chain = auditService.getChain("ENT-AUDIT");
        assertEquals(4, chain.size());

        // Verify chain integrity
        var verification = auditService.verifyChain("ENT-AUDIT");
        assertTrue(verification.valid());

        // Check event types in order: CERTIFICATE_ISSUED, ENTERPRISE_REGISTERED, CERTIFICATE_BOUND
        assertEquals("CERTIFICATE_ISSUED", chain.get(0).getEventType().name());
        assertEquals("ENTERPRISE_REGISTERED", chain.get(1).getEventType().name());
        assertEquals("CERTIFICATE_BOUND", chain.get(2).getEventType().name());
    }
}
