package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.identity.OidcPkiBindingService.BindingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DppIdentityBindingControllerTest {

    private DppIdentityBindingController controller;
    private com.battery.integration.dpp.audit.AuditService auditService;
    private com.battery.integration.dpp.certificate.CertificateManager certificateManager;
    private com.battery.integration.dpp.registry.DppRegistryService registryService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        var props = new com.battery.integration.dpp.config.DppRegistryProperties();
        props.setKeyStoreDir(tempDir.toString());

        auditService = new com.battery.integration.dpp.audit.AuditService(props);
        certificateManager = new com.battery.integration.dpp.certificate.CertificateManager(props);

        com.battery.integration.dpp.registry.DppRegistryClient mockClient =
                new com.battery.integration.dpp.registry.DppRegistryClient(props, null) {
            @Override
            public com.fasterxml.jackson.databind.JsonNode registerEnterprise(
                    String id, String name, String vat, String eori, String cc) {
                return new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode()
                        .put("registryId", "REG-EU-" + id);
            }
            @Override
            public com.fasterxml.jackson.databind.JsonNode bindCertificate(String id, String pem) {
                return new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode()
                        .put("status", "bound");
            }
        };

        registryService = new com.battery.integration.dpp.registry.DppRegistryService(
                props, mockClient, auditService);

        var bindingService = new com.battery.integration.dpp.identity.OidcPkiBindingService(
                certificateManager, registryService, auditService);

        controller = new DppIdentityBindingController(bindingService);
    }

    @Test
    void testBindIdentity() {
        Map<String, String> request = Map.of(
                "enterpriseId", "ENT-IDB-1",
                "legalName", "Identity Binding AG",
                "countryCode", "DE",
                "oidcSubject", "admin@identity.de",
                "oidcIssuer", "https://iam.identity.de"
        );

        @SuppressWarnings("unchecked")
        Result<BindingResult> result = controller.bindIdentity(request);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertNotNull(result.getData().certificate());
        assertNotNull(result.getData().registration());
        assertEquals("ENT-IDB-1", result.getData().certificate().getEnterpriseId());
        assertEquals("admin@identity.de", result.getData().registration().getBoundOidcSubject());
    }

    @Test
    void testBindIdentityMissingParams() {
        Map<String, String> request = Map.of(
                "enterpriseId", "ENT-IDB-2",
                "countryCode", "FR"
                // missing legalName, oidcSubject, oidcIssuer
        );

        @SuppressWarnings("unchecked")
        Result<BindingResult> result = controller.bindIdentity(request);
        assertEquals(400, result.getCode());
    }

    @Test
    void testVerifyBinding() throws Exception {
        // First bind the identity
        Map<String, String> bindRequest = Map.of(
                "enterpriseId", "ENT-VRFY-1",
                "legalName", "Verify Corp",
                "countryCode", "NL",
                "oidcSubject", "user@verify.nl",
                "oidcIssuer", "https://iam.verify.nl"
        );
        controller.bindIdentity(bindRequest);

        // Then verify
        Map<String, String> verifyRequest = Map.of(
                "enterpriseId", "ENT-VRFY-1",
                "oidcSubject", "user@verify.nl",
                "oidcIssuer", "https://iam.verify.nl"
        );

        @SuppressWarnings("unchecked")
        Result<Map<String, Object>> verifyResult = controller.verifyBinding(verifyRequest);
        assertEquals(200, verifyResult.getCode());
        assertEquals(true, verifyResult.getData().get("bindingVerified"));
    }

    @Test
    void testVerifyBindingWrongIdentity() throws Exception {
        Map<String, String> bindRequest = Map.of(
                "enterpriseId", "ENT-VRFY-2",
                "legalName", "Wrong Verify GmbH",
                "countryCode", "AT",
                "oidcSubject", "real@user.at",
                "oidcIssuer", "https://iam.real.at"
        );
        controller.bindIdentity(bindRequest);

        Map<String, String> wrongRequest = Map.of(
                "enterpriseId", "ENT-VRFY-2",
                "oidcSubject", "fake@user.at",
                "oidcIssuer", "https://iam.fake.at"
        );

        @SuppressWarnings("unchecked")
        Result<Map<String, Object>> verifyResult = controller.verifyBinding(wrongRequest);
        assertEquals(200, verifyResult.getCode());
        assertEquals(false, verifyResult.getData().get("bindingVerified"));
    }
}
