package com.battery.integration.dpp.controller;

import com.battery.common.Result;
import com.battery.integration.dpp.model.DppEnterprise;
import com.battery.integration.dpp.model.DppEnterprise.RegistrationStatus;
import com.battery.integration.dpp.model.DppRegistration;
import com.battery.integration.dpp.registry.DppRegistryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DppRegistryControllerTest {

    private DppRegistryController controller;
    private DppRegistryService registryService;
    private com.battery.integration.dpp.audit.AuditService auditService;
    private com.battery.integration.dpp.config.DppRegistryProperties props;

    @BeforeEach
    void setUp() {
        props = new com.battery.integration.dpp.config.DppRegistryProperties();
        auditService = new com.battery.integration.dpp.audit.AuditService(props);

        com.battery.integration.dpp.registry.DppRegistryClient mockClient =
                new com.battery.integration.dpp.registry.DppRegistryClient(props, null) {
            @Override
            public com.fasterxml.jackson.databind.JsonNode registerEnterprise(
                    String id, String name, String vat, String eori, String cc) {
                return new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode()
                        .put("registryId", "REG-" + id);
            }
            @Override
            public com.fasterxml.jackson.databind.JsonNode bindCertificate(String id, String pem) {
                return new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode()
                        .put("status", "bound");
            }
            @Override
            public com.fasterxml.jackson.databind.JsonNode revokeRegistration(String id, String reason) {
                return new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode()
                        .put("status", "revoked");
            }
        };

        registryService = new com.battery.integration.dpp.registry.DppRegistryService(
                props, mockClient, auditService);
        controller = new DppRegistryController(registryService);
    }

    @Test
    void testPreRegisterEnterprise() {
        DppEnterprise enterprise = new DppEnterprise("ENT-CTRL-1", "Controller Test AG",
                "DE123456789", "EORI-DE-001", "DE");

        Result<DppRegistration> result = controller.preRegisterEnterprise(enterprise);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("ENT-CTRL-1", result.getData().getEnterpriseId());
        assertEquals(RegistrationStatus.PENDING, result.getData().getStatus());
    }

    @Test
    void testSubmitToRegistry() {
        DppEnterprise enterprise = new DppEnterprise("ENT-CTRL-2", "Submit Test",
                "FR987654", "EORI-FR-001", "FR");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        Result<DppRegistration> result = controller.submitToRegistry(reg.getRegistrationId());
        assertEquals(200, result.getCode());
        assertNotNull(result.getData().getDppRegistryId());
    }

    @Test
    void testBindCertificate() {
        DppEnterprise enterprise = new DppEnterprise("ENT-CTRL-3", "Bind Test",
                "IT123456", "EORI-IT-001", "IT");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        Result<DppRegistration> result = controller.bindCertificate(
                reg.getRegistrationId(), "CERT-CTRL-1",
                "user@test.com", "https://iam.test.com");

        assertEquals(200, result.getCode());
        assertEquals("CERT-CTRL-1", result.getData().getBoundCertId());
        assertEquals("user@test.com", result.getData().getBoundOidcSubject());
    }

    @Test
    void testGetRegistration() {
        DppEnterprise enterprise = new DppEnterprise("ENT-CTRL-4", "Get Test",
                "ES111111", "EORI-ES-001", "ES");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        Result<DppRegistration> found = controller.getRegistration(reg.getRegistrationId());
        assertEquals(200, found.getCode());
        assertNotNull(found.getData());

        Result<DppRegistration> notFound = controller.getRegistration("NONEXISTENT");
        assertEquals(404, notFound.getCode());
    }

    @Test
    void testGetRegistrationByEnterprise() {
        DppEnterprise enterprise = new DppEnterprise("ENT-CTRL-5", "By Ent Test",
                "NL222222", "EORI-NL-001", "NL");
        registryService.preRegisterEnterprise(enterprise, "admin");

        Result<DppRegistration> found = controller.getRegistrationByEnterprise("ENT-CTRL-5");
        assertEquals(200, found.getCode());

        Result<DppRegistration> notFound = controller.getRegistrationByEnterprise("NONEXISTENT");
        assertEquals(404, notFound.getCode());
    }

    @Test
    void testListRegistrations() {
        for (int i = 1; i <= 3; i++) {
            DppEnterprise enterprise = new DppEnterprise("ENT-LIST-" + i, "List Test " + i,
                    "VAT" + i, "EORI" + i, "DE");
            registryService.preRegisterEnterprise(enterprise, "admin");
        }

        Result<List<DppRegistration>> result = controller.listRegistrations();
        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().size());
    }

    @Test
    void testConfirmRegistration() {
        DppEnterprise enterprise = new DppEnterprise("ENT-CTRL-6", "Confirm Test",
                "SE333333", "EORI-SE-001", "SE");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");

        Result<DppRegistration> result = controller.confirmRegistration(reg.getRegistrationId());
        assertEquals(200, result.getCode());
        assertEquals(RegistrationStatus.VERIFIED, result.getData().getStatus());
    }

    @Test
    void testRevokeRegistration() {
        DppEnterprise enterprise = new DppEnterprise("ENT-CTRL-7", "Revoke Test",
                "DK444444", "EORI-DK-001", "DK");
        DppRegistration reg = registryService.preRegisterEnterprise(enterprise, "admin");
        registryService.submitToRegistry(reg.getRegistrationId(), "admin");

        Result<DppRegistration> result = controller.revokeRegistration(
                reg.getRegistrationId(), "Regulatory non-compliance");
        assertEquals(200, result.getCode());
        assertEquals(RegistrationStatus.REVOKED, result.getData().getStatus());
    }
}
